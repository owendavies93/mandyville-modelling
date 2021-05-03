package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.common.entity.FPLGameweek
import es.odavi.mandyville.common.{
  FlywayConfig,
  InsertSchema,
  TestPostgresDatabase
}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.scalatest.Outcome
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.time.LocalDateTime

class GameweekDatabaseServiceSuite
    extends FixtureAnyFunSuite
    with DockerTestKit
    with TestPostgresDatabase
    with FlywayConfig {
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(client)

  case class FixtureParam(ctx: PostgresJdbcContext[SnakeCase] with InsertSchema)

  // TODO: Combine this with shared behaviour from other integration
  //       test suites
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

  test("Returns correct gameweek for season and gw number") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val season: Short = 2020
    val gwID: Short = 1
    val gw = FPLGameweek(1, season, gwID, LocalDateTime.now())

    ctx.run(insertFPLGameweek(gw))

    val manager = new GameweekManager(new GameweekDatabaseImp(ctx))

    val result = manager.getGameweek(season, gwID)

    assert(result.season === season)
    assert(result.gameweek === gwID)
  }
}
