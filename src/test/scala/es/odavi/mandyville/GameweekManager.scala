package es.odavi.mandyville

import es.odavi.mandyville.common.entity.FPLGameweek
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime

class GameweekManagerSuite extends AnyFunSuite with MockitoSugar {
  private val season: Short = 2020
  private val gw: Short = 1
  private val gameweek = FPLGameweek(1, season, gw, LocalDateTime.now())

  private val dbService = mock[GameweekDatabaseService]

  test("Correct gameweek returned from getGameweek") {
    when(dbService.getGameweek(season, gw)).thenReturn(gameweek)

    val result = new GameweekManager(dbService).getGameweek(season, gw)
    assert(result.season === season)
    assert(result.gameweek === gw)
  }
}
