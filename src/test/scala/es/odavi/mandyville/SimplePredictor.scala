package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.getDummyFixtureInfo
import es.odavi.mandyville.common.entity.{
  FPLGameweek,
  Fixture,
  Player,
  PlayerFixture
}
import org.mockito.MockitoSugar
import org.scalatest.PrivateMethodTester
import org.scalatest.funsuite.AnyFunSuite

import java.time.{LocalDate, LocalDateTime}

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
      .plusDays(1)

    val season = date.getYear.toShort

    val gameweek = FPLGameweek(1, season, 1, date.atStartOfDay())
    val context = Context(gameweek)

    val dbService = mock[PlayerDatabaseService]
    when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

    val predictor =
      new SimplePredictor(context, new PlayerManager(dbService))
    val fixtures = predictor.invokePrivate(getFixtures(player))

    assert(fixtures.size == testFixtures.count { case (_, f) => f.hasDate })
  }

  // Tests from here on in can share a context and predictor
  val gameweek = FPLGameweek(1, 2020, 1, LocalDateTime.now())
  val context = Context(gameweek)

  val dbService = mock[PlayerDatabaseService]
  when(dbService.getAllFixturesForPlayer(player)).thenReturn(testFixtures)

  val predictor =
    new SimplePredictor(context, new PlayerManager(dbService))

  test("getFixtures is cached") {
    predictor.invokePrivate(getFixtures(player))
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
    predictor.invokePrivate(getFixtures(player))
    verify(dbService, times(1)).getAllFixturesForPlayer(player)
  }

  test("chanceOfRedCard returns a decimal between 0 and 1") {
    val red = predictor.chanceOfRedCard(player)
    assert(red >= 0)
    assert(red <= 1)
  }

  test("chanceOfYellowCard returns a decimal between 0 and 1") {
    val yellow = predictor.chanceOfYellowCard(player)
    assert(yellow >= 0)
    assert(yellow <= 1)
  }

  test("goals, assists and conceded are all non-negative and non-zero") {
    assert(predictor.expectedAssists(player) > 0)
    assert(predictor.expectedGoals(player) > 0)
    assert(predictor.expectedConceded(player) > 0)
  }

  test("expectedMinutes returns a decimal between 0 and 90 inclusive") {
    val mins = predictor.expectedMinutes(player)
    assert(mins >= 0)
    assert(mins <= 90)
  }

  test("All calculations are pure functions") {
    val red = predictor.chanceOfRedCard(player)
    val yellow = predictor.chanceOfYellowCard(player)
    val mins = predictor.expectedMinutes(player)
    val conceded = predictor.expectedConceded(player)
    val goals = predictor.expectedGoals(player)
    val assists = predictor.expectedAssists(player)

    assert(red == predictor.chanceOfRedCard(player))
    assert(yellow == predictor.chanceOfYellowCard(player))
    assert(mins == predictor.expectedMinutes(player))
    assert(conceded == predictor.expectedConceded(player))
    assert(goals == predictor.expectedGoals(player))
    assert(assists == predictor.expectedAssists(player))
  }
}
