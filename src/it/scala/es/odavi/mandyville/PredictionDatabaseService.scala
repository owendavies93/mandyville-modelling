package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.TestUtils.getDummyPlayer
import es.odavi.mandyville.common.entity.{Country, FPLGameweek, Prediction}
import es.odavi.mandyville.common.{
  FlywayConfig,
  InsertSchema,
  TestPostgresDatabase
}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.scalatest.Outcome
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.time.LocalDateTime

class PredictionDatabaseServiceSuite
    extends FixtureAnyFunSuite
    with DockerTestKit
    with TestPostgresDatabase
    with FlywayConfig {
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(client)

  case class FixtureParam(ctx: PostgresJdbcContext[SnakeCase] with InsertSchema)

  override protected def withFixture(test: OneArgTest): Outcome = {
    val fixtureConfig = new HikariConfig()
    fixtureConfig.setJdbcUrl(dbUrl)
    fixtureConfig.setUsername(dbUser)
    fixtureConfig.setPassword(dbPassword)

    val dbSource = new HikariDataSource(fixtureConfig)

    val migrations = flyway.dataSource(dbSource).load()
    migrations.migrate()

    val ctx = new PostgresJdbcContext[SnakeCase](SnakeCase, dbSource)
      with InsertSchema

    withFixture(test.toNoArgTest(FixtureParam(ctx)))
  }

  test("Inserts and updates a prediction correctly") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val playerId = 1
    val country = Country(1, "Test", "TT")
    val player = getDummyPlayer(playerId, country.id)
    val gameweek = FPLGameweek(1, 2020, 1, LocalDateTime.now())

    ctx.run(insertCountry(country))
    ctx.run(insertPlayer(player))
    ctx.run(insertFPLGameweek(gameweek))

    val prediction = Prediction(1, playerId, gameweek.id, 1, 1.0)
    val manager = new PredictionManager(new PredictionDatabaseImp(ctx))

    manager.insertOrUpdatePrediction(prediction)

    val fromDb = getPrediction(ctx, prediction)

    assert(fromDb.size === 1)
    assert(fromDb.head.prediction === prediction.prediction)

    val newPrediction = 4.5
    val updatedPrediction = prediction.copy(prediction = newPrediction)

    manager.insertOrUpdatePrediction(updatedPrediction)

    val newFromDb = getPrediction(ctx, updatedPrediction)

    assert(newFromDb.size === 1)
    assert(newFromDb.head.prediction === updatedPrediction.prediction)
  }

  private def getPrediction(
    ctx: PostgresJdbcContext[SnakeCase] with InsertSchema,
    prediction: Prediction
  ): List[Prediction] = {
    import ctx._
    ctx.run(quote {
      predictions.filter(p =>
        p.playerId == lift(prediction.playerId) && p.fplGameweekId == lift(
          prediction.fplGameweekId
        ) && p.predictorId == lift(prediction.predictorId)
      )
    })
  }
}
