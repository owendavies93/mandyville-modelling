package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.TestUtils.getDummyPlayer
import es.odavi.mandyville.common.entity.Player
import es.odavi.mandyville.common.{
  Comparison,
  FlywayConfig,
  InsertSchema,
  TestPostgresDatabase
}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.scalatest.{BeforeAndAfterAll, Outcome}
import org.scalatest.funsuite.{AnyFunSuite, FixtureAnyFunSuite}

class ModelSuite
    extends FixtureAnyFunSuite
    with DockerTestKit
    with TestPostgresDatabase
    with FlywayConfig {
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(client)

  case class FixtureParam(
    ctx: PostgresJdbcContext[SnakeCase] with InsertSchema,
    dbSource: HikariDataSource
  )

  override protected def withFixture(test: OneArgTest): Outcome = {
    val fixtureConfig = new HikariConfig()
    fixtureConfig.setJdbcUrl(dbUrl)
    fixtureConfig.setUsername(dbUser)
    fixtureConfig.setPassword(dbPassword)

    val dbSource = new HikariDataSource(fixtureConfig)

    val ctx = new PostgresJdbcContext[SnakeCase](SnakeCase, dbSource)
      with InsertSchema

    withFixture(test.toNoArgTest(FixtureParam(ctx, dbSource)))
  }

  test("Test data loaded correctly") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    // TODO: Make this locations setting generic and move it into the
    //       config trait
    // TODO: Running this in the first test only works fine, but is a
    //       little grim. What's more, it only works because the tests
    //       aren't run in parallel. However, if you run this in
    //       withFixture, it runs at the start of every test
    val migrations = flyway.locations("sql").dataSource(fixture.dbSource).load()
    migrations.migrate()

    val limit = 5

    val result = ctx.run(quote {
      players.take(lift(limit))
    })

    assert(result.size === limit)
  }

  test("Model predictions are correctly run") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val season: Short = 2020
    val gw: Short = 10
    val gameweekManager = new GameweekManager(new GameweekDatabaseImp(ctx))

    val gameweek = gameweekManager.getGameweek(season, gw)
    val context = Context(gameweek)
    val playerManager = new PlayerManager(new PlayerDatabaseImp(ctx))
    val predictionManager =
      new PredictionManager(new PredictionDatabaseImp(ctx))

    val model = new Model(context, playerManager, predictionManager)

    def factory(c: Context, m: PlayerManager): SimplePredictor =
      new SimplePredictor(c, m)

    val predictions = model.runPredictions[SimplePredictor](factory)

    val playerCount = ctx.run(quote(players.map(_.id)).size)

    assert(predictions.size === playerCount)
  }

  test("Predictions are correctly stored") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val season: Short = 2020
    val gw: Short = 10
    val gameweekManager = new GameweekManager(new GameweekDatabaseImp(ctx))

    val gameweek = gameweekManager.getGameweek(season, gw)
    val context = Context(gameweek)
    val playerManager = new PlayerManager(new PlayerDatabaseImp(ctx))
    val predictionManager =
      new PredictionManager(new PredictionDatabaseImp(ctx))

    val model = new Model(context, playerManager, predictionManager)

    val predictor = new SimplePredictor(context, playerManager)

    val expected = 4.5
    val actual = 3.4
    val player = ctx
      .run(quote {
        players
      }.take(1))
      .head

    val comparison = Comparison(
      player,
      context,
      predictor,
      expected,
      actual
    )

    model.storePredictions(Set(comparison))

    val fromDb = ctx.run(quote {
      predictions
    })
    assert(fromDb.size === 1)
    assert(fromDb.head.prediction === expected)
  }
}
