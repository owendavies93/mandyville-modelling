package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.TestUtils.{getDummyFixtureInfo, getDummyPlayer}
import es.odavi.mandyville.common.entity.{
  Competition,
  Country,
  FPLGameweek,
  PlayerFixture,
  Team
}
import es.odavi.mandyville.common.{FlywayConfig, Schema, TestPostgresDatabase}
import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.scalatest.Outcome
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.time.LocalDateTime

class PlayerDatabaseServiceSuite
    extends FixtureAnyFunSuite
    with DockerTestKit
    with TestPostgresDatabase
    with FlywayConfig {
  private val client: DockerClient = DefaultDockerClient.fromEnv().build()

  override implicit def dockerFactory: DockerFactory =
    new SpotifyDockerFactory(client)

  case class FixtureParam(ctx: PostgresJdbcContext[SnakeCase] with Schema)

  override protected def withFixture(test: OneArgTest): Outcome = {
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

  test("Returns correct fixtures for player") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val playerId = 1
    val playerTeam = Team(1, "Test", 1)
    val otherTeam = Team(2, "Test", 2)
    val country = Country(1, "Test", "TT")
    val player = getDummyPlayer(playerId, country.id)
    val comp = Competition(1, "Test", country.id, Some(1), Some(1))
    val (pf, f) =
      getDummyFixtureInfo(playerId, playerTeam.id, otherTeam.id, comp.id)

    ctx.run(insertCountry(country))
    ctx.run(insertCompetition(comp))
    ctx.run(insertTeam(playerTeam))
    ctx.run(insertTeam(otherTeam))
    ctx.run(insertPlayer(player))

    // TODO: Is there a nicer way of doing this? Possibly separating
    //       out the generation of dummy PlayerFixture and Fixture
    val fId = ctx.run(insertFixture(f))
    val pf2: PlayerFixture = pf.copy(fixtureId = fId)

    ctx.run(insertPlayerFixture(pf2))

    /*
    val gameweek = FPLGameweek(1, 2020, 1, LocalDateTime.now())
    val context = Context(gameweek)
    val manager = new PlayerManager
    val relevantFixtures = manager.getRelevantFixtures(player, context)

    assert(relevantFixtures.size === 1)
    assert(relevantFixtures.head._2.id === f.id)
     */
  }
}
