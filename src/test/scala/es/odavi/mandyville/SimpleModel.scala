package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.getDummyFixtureInfo
import es.odavi.mandyville.common.entity.{
  FPLGameweek,
  Fixture,
  Player,
  PlayerFixture
}
import org.joda.time.{DateTime, DurationFieldType, LocalDate}
import org.mockito.MockitoSugar
import org.scalatest.PrivateMethodTester
import org.scalatest.funsuite.AnyFunSuite

class SimpleModelSuite
    extends AnyFunSuite
    with MockitoSugar
    with PrivateMethodTester {
  private val playerId = 1
  private val teamId = 1

  private val player = Player(playerId, "Owen", "Davies", 1, None, None, None)

  private val testFixtures =
    (1 to 10).map(_ => getDummyFixtureInfo(playerId, teamId)).toList

  private val getFixtures =
    PrivateMethod[List[(PlayerFixture, Fixture)]]('getFixtures)

  test("All fixtures used for context after all fixtures") {
    // Find the last date in the generated set of dates, and then add
    // a day to ensure that all fixtures are counted as 'previous'
    // fixtures
    val date: LocalDate = testFixtures
      .filter { case (_, f) => f.hasDate }
      .maxBy { case (_, f) => f.fixtureDate.get }
      ._2
      .fixtureDate
      .get
      .withFieldAdded(DurationFieldType.days(), 1)

    val season = date.getYear.toShort

    val gameweek = FPLGameweek(1, season, 1, date.toDateTimeAtStartOfDay)
    val context = Context(gameweek)

    val dbService = mock[PlayerDatabaseService]
    when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

    val model = new SimpleModel(player, context, new PlayerManager(dbService))
    val fixtures = model.invokePrivate(getFixtures())

    assert(fixtures.size == testFixtures.count { case (_, f) => f.hasDate })
  }

  test("getFixtures is cached") {
    val gameweek = FPLGameweek(1, 2020, 1, DateTime.now())
    val context = Context(gameweek)

    val dbService = mock[PlayerDatabaseService]
    when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

    val model = new SimpleModel(player, context, new PlayerManager(dbService))
    model.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
    model.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
  }
}
