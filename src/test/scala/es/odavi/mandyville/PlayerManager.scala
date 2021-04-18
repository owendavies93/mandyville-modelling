package es.odavi.mandyville

import es.odavi.mandyville.common.entity.PositionCategory.Goalkeeper
import es.odavi.mandyville.common.entity.{FPLGameweek, FPLSeasonInfo, Player}
import org.joda.time.DateTime
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

class PlayerManagerSuite extends AnyFunSuite with MockitoSugar {
  val dbService = mock[PlayerDatabaseService]
  val season: Short = 2020
  val goalkeeperId: Short = 1

  val seasonInfo = FPLSeasonInfo(1, 1, season, 1, goalkeeperId)
  val player = Player(1, "Owen", "Davies", 1, None, None, None)
  val gameweek = FPLGameweek(1, season, 1, DateTime.now())
  val context = Context(gameweek)

  when(dbService.getSeasonInfoForPlayer(player, context)).thenReturn(seasonInfo)

  test("Returns correct position from Position Map") {
    assertResult(Goalkeeper) {
      new PlayerManager(dbService).getPositionForPlayer(player, context)
    }
  }
}
