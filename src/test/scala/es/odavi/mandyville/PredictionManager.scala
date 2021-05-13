package es.odavi.mandyville

import es.odavi.mandyville.TestUtils.getDummyPrediction
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

class PredictionManagerSuite extends AnyFunSuite with MockitoSugar {
  private val dbService = mock[PredictionDatabaseService]

  val playerId = 1
  val fplGameweekId = 1

  val prediction = getDummyPrediction(playerId, fplGameweekId)
  val databaseId = 12

  when(dbService.insertOrUpdatePrediction(prediction)).thenReturn(databaseId)

  test("Returns correct id when inserting a prediction") {
    val fromManager =
      new PredictionManager(dbService).insertOrUpdatePrediction(prediction)
    assert(fromManager.id === databaseId)
  }
}
