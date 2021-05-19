package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.getDummyPerformance
import es.odavi.mandyville.common.entity.PositionCategory.Goalkeeper
import es.odavi.mandyville.common.entity.{
  FPLGameweek,
  FPLPosition,
  FPLSeasonInfo,
  Player
}
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime

class PredictorSuite extends AnyFunSuite with MockitoSugar {
  private val player = Player(1, "Owen", "Davies", 1, None, None, None)
  private val gameweek = FPLGameweek(1, 2020, 1, LocalDateTime.now())
  private val context = Context(gameweek)
  private val dbService = mock[PlayerDatabaseService]
  private val seasonInfo = FPLSeasonInfo(1, 1, 2020, 1, 1)
  private val position = FPLPosition(1, 1, Goalkeeper)

  when(dbService.getSeasonInfoForPlayer(player, context))
    .thenReturn((seasonInfo, position))

  val playerManager = new PlayerManager(dbService)
  val assists: BigDecimal = 1.0
  val goals: BigDecimal = 1.0

  class NoPlayPredictorStub extends Predictor(context, playerManager) {
    override def chanceOfRedCard(p: Player) = 0
    override def chanceOfYellowCard(p: Player) = 0.4
    override def expectedAssists(p: Player): BigDecimal = assists
    override def expectedConceded(p: Player) = 3.0
    override def expectedGoals(p: Player): BigDecimal = goals
    override def expectedMinutes(p: Player) = 0
    override def id = 1
  }

  test("Returns zero points for zero minutes") {
    assertResult(0) {
      val noPlay = new NoPlayPredictorStub()
      noPlay.pointsForGameweek(player)
    }
  }

  class Full90PredictorStub extends NoPlayPredictorStub {
    override def expectedMinutes(p: Player) = 90
  }

  test("Returns correct points for full 90 minutes prediction") {
    // -1 for conceded goals
    // 2 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) - 1 - 0.4 + 2

    assertResult(expectedPoints) {
      val full90 = new Full90PredictorStub()
      full90.pointsForGameweek(player)
    }
  }

  class Full90CleanSheetPredictor extends Full90PredictorStub {
    override def expectedConceded(p: Player) = 0.4
  }

  test("Returns correct points for clean sheet") {
    // 2 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) +
        CleanSheetPoints(Goalkeeper) - 0.4 + 2

    assertResult(expectedPoints) {
      val cleanSheet = new Full90CleanSheetPredictor()
      cleanSheet.pointsForGameweek(player)
    }
  }

  class SubPredictor extends NoPlayPredictorStub {
    override def expectedMinutes(p: Player) = 15
  }

  test("Returns correct points total for substitute") {
    // 1 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) - 0.4 + 1

    assertResult(expectedPoints) {
      val sub = new SubPredictor()
      sub.pointsForGameweek(player)
    }
  }

  test("comparePrediction returns comparison") {
    val actualPerformance = getDummyPerformance(player.id, gameweek.id)
    when(dbService.getFPLPerformance(player, context))
      .thenReturn(List(actualPerformance))

    val predictor = new Full90PredictorStub()
    val evaluation = predictor.comparePrediction(player)

    assert(evaluation.difference <= evaluation.expected + evaluation.actual)
  }

  private val newId = 2
  class DifferentIDPredictor extends SubPredictor {
    override def id: Int = newId
  }

  test("Correct predictor ID is passed to Comparison object") {
    val predictor = new DifferentIDPredictor()

    val evaluation = predictor.comparePrediction(player)
    assert(evaluation.predictor.id === newId)
  }
}
