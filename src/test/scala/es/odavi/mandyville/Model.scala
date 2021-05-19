package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.{getDummyPerformance, randomBigDecimal}
import es.odavi.mandyville.common.entity.{FPLGameweek, Player}
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime

class ModelSuite extends AnyFunSuite with MockitoSugar {
  private val playerId = 1
  private val gw = 1
  private val season: Short = 2020
  private val player = Player(playerId, "Test", "Test", 1, None, None, Some(1))

  private val player2 =
    Player(playerId + 1, "Test2", "Test2", 1, None, None, Some(2))

  private val gameweek =
    FPLGameweek(gw, season, gw.toShort, LocalDateTime.now())
  private val context = Context(gameweek)

  private val dbService = mock[PlayerDatabaseService]
  when(dbService.getAllPlayersForSeason(season)).thenReturn(List(player))

  private val performances = List(getDummyPerformance(playerId, gw))
  when(dbService.getFPLPerformance(player, context)).thenReturn(performances)

  private val playerManager = new PlayerManager(dbService)

  private val predDbService = mock[PredictionDatabaseService]

  private val predictionManager = new PredictionManager(predDbService)

  class PredictorStub(c: Context, pm: PlayerManager)
      extends SimplePredictor(c, pm) {
    override def pointsForGameweek(p: Player): BigDecimal = p.id * 3
  }

  def factory(c: Context, pm: PlayerManager) =
    new PredictorStub(c, pm)

  test("runPredictions returns correct predictions and performances") {
    val predictions = new Model(context, playerManager, predictionManager)
      .runPredictions(factory)

    assert(predictions.size === 1)
    assert(predictions.head.actual === performances.head.totalPoints)
  }

  test("only one prediction per player is returned for a given context") {
    when(dbService.getAllPlayersForSeason(season))
      .thenReturn(List(player, player))

    val predictions = new Model(context, playerManager, predictionManager)
      .runPredictions(factory)

    assert(predictions.size === 1)
  }

  test("players with identical predictions have separate entries") {
    when(dbService.getAllPlayersForSeason(season))
      .thenReturn(List(player, player2))
    when(dbService.getFPLPerformance(player2, context)).thenReturn(performances)

    val model = new Model(context, playerManager, predictionManager)
    val predictions = model.runPredictions(factory)

    assert(predictions.size === 2)
  }

  test("correlation coefficient is between 1 and -1") {
    when(dbService.getAllPlayersForSeason(season))
      .thenReturn(List(player, player2))
    val perf2 = List(getDummyPerformance(player2.id, gw))
    when(dbService.getFPLPerformance(player2, context)).thenReturn(perf2)

    val model = new Model(context, playerManager, predictionManager)
    val predictions = model.runPredictions(factory)

    val corr = model.correlatePredictions(predictions)
    assert(corr >= -1)
    assert(corr <= 1)
  }
}
