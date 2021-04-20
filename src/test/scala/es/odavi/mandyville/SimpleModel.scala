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

  // Tests from here on in can share a context and model
  val gameweek = FPLGameweek(1, 2020, 1, DateTime.now())
  val context = Context(gameweek)

  val dbService = mock[PlayerDatabaseService]
  when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

  val model = new SimpleModel(player, context, new PlayerManager(dbService))

  test("getFixtures is cached") {
    model.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
    model.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
  }

  test("chanceOfRedCard returns a decimal between 0 and 1") {
    val red = model.chanceOfRedCard()
    assert(red >= 0)
    assert(red <= 1)
  }

  test("chanceOfYellowCard returns a decimal between 0 and 1") {
    val yellow = model.chanceOfYellowCard()
    assert(yellow >= 0)
    assert(yellow <= 1)
  }

  test("goals, assists and conceded are all non-negative and non-zero") {
    assert(model.expectedAssists() > 0)
    assert(model.expectedGoals() > 0)
    assert(model.expectedConceded() > 0)
  }

  test("expectedMinutes returns a decimal between 0 and 90 inclusive") {
    val mins = model.expectedMinutes()
    assert(mins >= 0)
    assert(mins <= 90)
  }

  test("All calculations are pure functions") {
    val red = model.chanceOfRedCard()
    val yellow = model.chanceOfYellowCard()
    val mins = model.expectedMinutes()
    val conceded = model.expectedConceded()
    val goals = model.expectedGoals()
    val assists = model.expectedAssists()

    assert(red == model.chanceOfRedCard())
    assert(yellow == model.chanceOfYellowCard())
    assert(mins == model.expectedMinutes())
    assert(conceded == model.expectedConceded())
    assert(goals == model.expectedGoals())
    assert(assists == model.expectedAssists())
  }
}
