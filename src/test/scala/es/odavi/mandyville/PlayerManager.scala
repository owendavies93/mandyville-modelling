package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.getDummyFixtureInfo
import es.odavi.mandyville.common.entity.PositionCategory.Goalkeeper
import es.odavi.mandyville.common.entity.{FPLGameweek, FPLSeasonInfo, Player}
import org.joda.time.LocalDate
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

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
}
