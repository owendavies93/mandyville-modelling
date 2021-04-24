package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.common.entity.PlayerFixture
import es.odavi.mandyville.common.{FlywayConfig, Schema, TestPostgresDatabase}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.scalatest.Outcome
import org.scalatest.funsuite.FixtureAnyFunSuite

class IntegrationFixtureSuite
    extends FixtureAnyFunSuite
    with DockerTestKit
    with TestPostgresDatabase
    with FlywayConfig {

  private val client: DockerClient = DefaultDockerClient.fromEnv().build()

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(client)

  case class FixtureParam(ctx: PostgresJdbcContext[SnakeCase] with Schema)

  override protected def withFixture(test: OneArgTest): Outcome = {
    // TODO: Can some of this be moved out into a separate trait?
    //       It could probably be included in the DatabaseConfig trait
    val fixtureConfig = new HikariConfig()
    fixtureConfig.setJdbcUrl(dbUrl)
    fixtureConfig.setUsername(dbUser)
    fixtureConfig.setPassword(dbPassword)

    val dbSource = new HikariDataSource(fixtureConfig)

    val migrations = flyway.dataSource(dbSource).load()
    migrations.migrate()

    val ctx = new PostgresJdbcContext[SnakeCase](SnakeCase, dbSource)
      with Schema

    withFixture(test.toNoArgTest(FixtureParam(ctx)))
  }

  test("Containers are all ready when tests start") { _ =>
    assert(dockerContainers.forall(_.isReady().futureValue))
  }

  test("Can connect to test database and run queries") { fixture =>
    import fixture.ctx
    import fixture.ctx._
    val result: List[PlayerFixture] = ctx.run(quote {
      playersFixtures.filter(p => p.id == 1)
    })

    assert(result.size === 0)
  }
}
