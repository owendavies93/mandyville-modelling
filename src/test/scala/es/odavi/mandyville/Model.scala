package es.odavi.mandyville

import com.github.nscala_time.time.Imports.DateTime
import es.odavi.mandyville.TestUtils.getDummyPerformance
import es.odavi.mandyville.common.entity.PositionCategory.Goalkeeper
import es.odavi.mandyville.common.entity.{FPLGameweek, FPLSeasonInfo, Player}
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

class ModelSuite extends AnyFunSuite with MockitoSugar {
  private val player = Player(1, "Owen", "Davies", 1, None, None, None)
  private val gameweek = FPLGameweek(1, 2020, 1, DateTime.now())
  private val context = Context(gameweek)
  private val dbService = mock[PlayerDatabaseService]
  private val seasonInfo = FPLSeasonInfo(1, 1, 2020, 1, 1)

  when(dbService.getSeasonInfoForPlayer(player, context)).thenReturn(seasonInfo)

  val playerManager = new PlayerManager(dbService)
  val assists: BigDecimal = 1.0
  val goals: BigDecimal = 1.0

  class NoPlayModelStub extends Model(player, context, playerManager) {
    override def chanceOfRedCard() = 0
    override def chanceOfYellowCard() = 0.4
    override def expectedAssists(): BigDecimal = assists
    override def expectedConceded() = 3.0
    override def expectedGoals(): BigDecimal = goals
    override def expectedMinutes() = 0
  }

  test("Returns zero points for zero minutes") {
    assertResult(0) {
      val noPlay = new NoPlayModelStub()
      noPlay.pointsForGameweek()
    }
  }

  class Full90ModelStub extends NoPlayModelStub {
    override def expectedMinutes() = 90
  }

  test("Returns correct points for full 90 minutes prediction") {
    // -1 for conceded goals
    // 2 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) - 1 - 0.4 + 2

    assertResult(expectedPoints) {
      val full90 = new Full90ModelStub()
      full90.pointsForGameweek()
    }
  }

  class Full90CleanSheetModel extends Full90ModelStub {
    override def expectedConceded() = 0.4
  }

  test("Returns correct points for clean sheet") {
    // 2 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) +
        CleanSheetPoints(Goalkeeper) - 0.4 + 2

    assertResult(expectedPoints) {
      val cleanSheet = new Full90CleanSheetModel()
      cleanSheet.pointsForGameweek()
    }
  }

  class SubModel extends NoPlayModelStub {
    override def expectedMinutes() = 15
  }

  test("Returns correct points total for substitute") {
    // 1 for appearance
    // -0.4 for yellow card penalty
    val expectedPoints =
      assists * AssistPoints + goals * GoalPoints(Goalkeeper) - 0.4 + 1

    assertResult(expectedPoints) {
      val sub = new SubModel()
      sub.pointsForGameweek()
    }
  }

  test("comparePrediction returns comparison") {
    val actualPerformance = getDummyPerformance(player.id, gameweek.id)
    when(dbService.getFPLPerformance(player, context))
      .thenReturn(List(actualPerformance))

    val model = new Full90ModelStub()
    val evaluation = model.comparePrediction()

    assert(evaluation.difference < evaluation.expected + evaluation.actual)
  }
}
