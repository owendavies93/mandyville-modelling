package es.odavi.mandyville

import es.odavi.mandyville.common.entity.FPLGameweek

/** A generic interface for interacting with a quill context for
  * fetching gameweek information from the mandyville database
  */
trait GameweekDatabaseService {
  def getGameweek(season: Short, gameweek: Short): FPLGameweek
}

/** An implementation of GameweekDatabaseService using common.Database
  * to fetch information from the mandyville database
  */
private class GameweekDatabaseImp extends GameweekDatabaseService {
  import es.odavi.mandyville.common.Database.ctx
  import es.odavi.mandyville.common.Database.ctx._

  override def getGameweek(season: Short, gameweek: Short): FPLGameweek = {
    val q = quote {
      fplGameweeks.filter(f =>
        f.season == lift(season) && f.gameweek == lift(gameweek)
      )
    }

    ctx.run(q).head
  }
}

/** Provides methods for managing gameweeks and fetching information
  * about them from the mandyville database
  *
  * @param service an implementation of GameweekDatabaseService
  */
class GameweekManager(
  service: GameweekDatabaseService = new GameweekDatabaseImp
) {

  /** Get the gameweek stored in the database associated with the given
    * season and gameweek number
    *
    * @param season the season we're interested in
    * @param gameweek the gameweek number we're interested in
    * @return an instance of FPLGameweek
    */
  def getGameweek(season: Short, gameweek: Short): FPLGameweek =
    service.getGameweek(season, gameweek)
}
