package es.odavi.mandyville

import es.odavi.mandyville.common.entity.PositionCategory._
import es.odavi.mandyville.common.entity.{
  FPLSeasonInfo,
  Player,
  PlayerFixture,
  PositionCategory
}

/** A generic interface for interacting with a quill context for
  * player related tasks
  */
trait PlayerDatabaseService {
  def getFixturesForPlayer(player: Player): List[PlayerFixture]
  def getSeasonInfoForPlayer(player: Player, context: Context): FPLSeasonInfo
}

/** An implementation of PlayerDatabaseService using common.Database
  * to fetch information from the mandyville database
  */
private class PlayerDatabaseImp extends PlayerDatabaseService {
  import es.odavi.mandyville.common.Database.ctx
  import es.odavi.mandyville.common.Database.ctx._

  // TODO: Use context here
  def getFixturesForPlayer(player: Player): List[PlayerFixture] =
    ctx.run(quote {
      playersFixtures.filter(p => p.playerId == lift(player.id))
    })

  def getSeasonInfoForPlayer(
    player: Player,
    context: Context
  ): FPLSeasonInfo = {
    val season = context.gameweek.season
    val q = quote {
      fplSeasonInfo.filter(f =>
        f.playerId == lift(player.id) && f.season == lift(season)
      )
    }

    ctx.run(q).head
  }
}

/** Provides methods for managing players and fetching information
  * about them from the mandyville database
  */
class PlayerManager(service: PlayerDatabaseService) {

  // TODO: Remove this once the position category decoding is
  //       worked out
  val positionMap: Map[Int, PositionCategory.Value] = Map(
    1 -> Goalkeeper,
    2 -> Defender,
    3 -> Midfielder,
    4 -> Forward
  )

  /** Get the FPL position for the player in the given context
    *
    * @param player the player we're interested in
    * @param context the context in which we want to find the position
    */
  def getPositionForPlayer(
    player: Player,
    context: Context
  ): PositionCategory.Value = {
    val info = service.getSeasonInfoForPlayer(player, context)

    positionMap(info.fplSeasonId)
  }
}
