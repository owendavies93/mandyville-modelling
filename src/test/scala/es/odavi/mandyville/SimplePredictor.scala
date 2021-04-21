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

class SimplePredictorSuite
    extends AnyFunSuite
    with MockitoSugar
    with PrivateMethodTester {
  private val playerId = 1
  private val teamId = 1

  private val player = Player(playerId, "Owen", "Davies", 1, None, None, None)

  private val testFixtures =
    (1 to 10).map(_ => getDummyFixtureInfo(playerId, teamId)).toList

  private val getFixtures =
    PrivateMethod[List[(PlayerFixture, Fixture)]](Symbol("getFixtures"))

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

    val predictor =
      new SimplePredictor(player, context, new PlayerManager(dbService))
    val fixtures = predictor.invokePrivate(getFixtures())

    assert(fixtures.size == testFixtures.count { case (_, f) => f.hasDate })
  }

  // Tests from here on in can share a context and predictor
  val gameweek = FPLGameweek(1, 2020, 1, DateTime.now())
  val context = Context(gameweek)

  val dbService = mock[PlayerDatabaseService]
  when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

  val predictor =
    new SimplePredictor(player, context, new PlayerManager(dbService))

  test("getFixtures is cached") {
    predictor.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
    predictor.invokePrivate(getFixtures())
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
  }

  test("chanceOfRedCard returns a decimal between 0 and 1") {
    val red = predictor.chanceOfRedCard()
    assert(red >= 0)
    assert(red <= 1)
  }

  test("chanceOfYellowCard returns a decimal between 0 and 1") {
    val yellow = predictor.chanceOfYellowCard()
    assert(yellow >= 0)
    assert(yellow <= 1)
  }

  test("goals, assists and conceded are all non-negative and non-zero") {
    assert(predictor.expectedAssists() > 0)
    assert(predictor.expectedGoals() > 0)
    assert(predictor.expectedConceded() > 0)
  }

  test("expectedMinutes returns a decimal between 0 and 90 inclusive") {
    val mins = predictor.expectedMinutes()
    assert(mins >= 0)
    assert(mins <= 90)
  }

  test("All calculations are pure functions") {
    val red = predictor.chanceOfRedCard()
    val yellow = predictor.chanceOfYellowCard()
    val mins = predictor.expectedMinutes()
    val conceded = predictor.expectedConceded()
    val goals = predictor.expectedGoals()
    val assists = predictor.expectedAssists()

    assert(red == predictor.chanceOfRedCard())
    assert(yellow == predictor.chanceOfYellowCard())
    assert(mins == predictor.expectedMinutes())
    assert(conceded == predictor.expectedConceded())
    assert(goals == predictor.expectedGoals())
    assert(assists == predictor.expectedAssists())
  }
}
