package es.odavi.mandyville

import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import com.whisk.docker.DockerFactory
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import es.odavi.mandyville.TestUtils.{getDummyFixtureInfo, getDummyPlayer}
import es.odavi.mandyville.common.entity._
import es.odavi.mandyville.common.{
  FlywayConfig,
  InsertSchema,
  TestPostgresDatabase
}
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

  case class FixtureParam(
    ctx: PostgresJdbcContext[SnakeCase] with InsertSchema,
    player: Player,
    competition: Competition
  )

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

    import ctx._

    val playerId = 1
    val country = Country(1, "Test", "TT")
    val player = getDummyPlayer(playerId, country.id)
    val comp = Competition(1, "Test", country.id, Some(1), Some(1))
    ctx.run(insertCountry(country))
    ctx.run(insertCompetition(comp))
    ctx.run(insertPlayer(player))

    withFixture(test.toNoArgTest(FixtureParam(ctx, player, comp)))
  }

  test("Returns correct fixtures for player") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val playerTeam = Team(1, "Test", 1)
    val otherTeam = Team(2, "Test", 2)
    val (pf, f) =
      getDummyFixtureInfo(
        fixture.player.id,
        playerTeam.id,
        otherTeam.id,
        fixture.competition.id
      )

    ctx.run(insertTeam(playerTeam))
    ctx.run(insertTeam(otherTeam))

    // TODO: Is there a nicer way of doing this? Possibly separating
    //       out the generation of dummy PlayerFixture and Fixture
    val fId = ctx.run(insertFixture(f))
    val pf2: PlayerFixture = pf.copy(fixtureId = fId)

    ctx.run(insertPlayerFixture(pf2))

    val gameweek = FPLGameweek(1, 2020, 1, LocalDateTime.now())
    val context = Context(gameweek)
    val manager = new PlayerManager(new PlayerDatabaseImp(ctx))
    val relevantFixtures = manager.getRelevantFixtures(fixture.player, context)

    assert(relevantFixtures.size === 1)
    assert(relevantFixtures.head._2.id === fId)
  }

  test("Returns correct FPL position for player") { fixture =>
    import fixture.ctx
    import fixture.ctx._

    val season: Short = 2020
    val fplPositionId: Short = 1
    val seasonInfo =
      FPLSeasonInfo(1, fixture.player.id, season, 1, fplPositionId)
    val fplPosition = FPLPosition(1, fplPositionId, PositionCategory.Goalkeeper)
    val gameweek = FPLGameweek(1, season, 1, LocalDateTime.now())
    val context = Context(gameweek)

    ctx.run(insertFPLPosition(fplPosition))
    ctx.run(insertFPLSeasonInfo(seasonInfo))

    val manager = new PlayerManager(new PlayerDatabaseImp(ctx))

    val position = manager.getPositionForPlayer(fixture.player, context)

    assert(position === PositionCategory.Goalkeeper)
  }
}
