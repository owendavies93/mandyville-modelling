package es.odavi.mandyville

import es.odavi.mandyville.common.entity.PositionCategory.Goalkeeper
import es.odavi.mandyville.common.entity.{
  FPLGameweek,
  FPLSeasonInfo,
  Fixture,
  Player,
  PlayerFixture
}
import org.joda.time.LocalDate
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Random

class PlayerManagerSuite extends AnyFunSuite with MockitoSugar {
  private val dbService = mock[PlayerDatabaseService]
  val season: Short = 2020
  val goalkeeperId: Short = 1
  val playerId = 1

  private val seasonInfo = FPLSeasonInfo(1, 1, season, 1, goalkeeperId)
  private val player = Player(playerId, "Owen", "Davies", 1, None, None, None)
  private val today = LocalDate.parse("2020-01-01")
  private val gameweek = FPLGameweek(1, season, 1, today.toDateTimeAtStartOfDay)
  private val context = Context(gameweek)

  when(dbService.getSeasonInfoForPlayer(player, context)).thenReturn(seasonInfo)

  test("Returns correct position from Position Map") {
    assertResult(Goalkeeper) {
      new PlayerManager(dbService).getPositionForPlayer(player, context)
    }
  }

  private val testFixtureDates =
    List[Option[String]](Option("2020-01-02"), None, Option("2019-01-01"))

  private val fixtureInfo = testFixtureDates.map { dateString =>
    val date =
      if (dateString.isDefined) Option(LocalDate.parse(dateString.get))
      else None
    getDummyFixtureInfo(playerId, 10, date, season)
  }

  when(dbService.getAllFixturesForPlayer(player)).thenReturn(fixtureInfo)

  test("Filters to relevant fixtures for the context") {
    val relevantFixtures =
      new PlayerManager(dbService).getRelevantFixtures(player, context)
    assert(relevantFixtures.size == 1)
  }

  // Currently doesn't generate fixture stats as the tests don't
  // require them
  def getDummyFixtureInfo(
    playerId: Int,
    teamId: Int,
    date: Option[LocalDate],
    season: Short
  ): (PlayerFixture, Fixture) = {
    val playerFixtureId = Random.between(1, 10000)
    val fixtureId = Random.between(1, 10000)
    (
      PlayerFixture(
        playerFixtureId,
        playerId,
        fixtureId,
        teamId,
        90,
        false,
        false,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None
      ),
      Fixture(fixtureId, 1, teamId, 1, season, None, None, None, date)
    )
  }
}
