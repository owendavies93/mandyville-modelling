package es.odavi.mandyville

import es.odavi.mandyville.common.{Database, Schema}
import es.odavi.mandyville.common.entity.PositionCategory._
import es.odavi.mandyville.common.entity._
import io.getquill.{PostgresJdbcContext, SnakeCase}

/** A generic interface for interacting with a quill context for
  * player related tasks
  */
trait PlayerDatabaseService {
  def getAllFixturesForPlayer(player: Player): List[(PlayerFixture, Fixture)]

  def getAllPlayersForSeason(season: Short): List[Player]

  def getFPLPerformance(
    player: Player,
    context: Context
  ): List[FPLPlayerGameweek]

  def getSeasonInfoForPlayer(player: Player, context: Context): FPLSeasonInfo
}

/** An implementation of PlayerDatabaseService using common.Database
  * to fetch information from the mandyville database
  */
class PlayerDatabaseImp(
  ctx: PostgresJdbcContext[SnakeCase] with Schema = Database.ctx
) extends PlayerDatabaseService {
  import ctx._

  def getAllFixturesForPlayer(player: Player): List[(PlayerFixture, Fixture)] =
    ctx.run(quote {
      playersFixtures
        .join(fixtures)
        .on(_.fixtureId == _.id)
        .filter { case (p, _) => p.playerId == lift(player.id) }
    })

  override def getAllPlayersForSeason(season: Short): List[Player] =
    ctx.run(quote {
      for {
        p <- players.distinct
        pg <- fplPlayersGameweeks.join(pg => pg.playerId == p.id)
        g <-
          fplGameweeks
            .join(g => g.id == pg.fplGameweekId)
            .filter(g => g.season == 2020)
      } yield p
    })

  override def getFPLPerformance(
    player: Player,
    context: Context
  ): List[FPLPlayerGameweek] =
    ctx.run(quote {
      fplPlayersGameweeks.filter(f =>
        f.playerId == lift(player.id) && f.fplGameweekId == lift(
          context.gameweek.id
        )
      )
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
class PlayerManager(service: PlayerDatabaseService = new PlayerDatabaseImp) {

  // TODO: Remove this once the position category decoding is
  //       worked out
  val positionMap: Map[Int, PositionCategory.Value] = Map(
    1 -> Goalkeeper,
    2 -> Defender,
    3 -> Midfielder,
    4 -> Forward
  )

  /** Get all players that are active in the FPL game in a given
    * season
    *
    * @param season the season we're interested in
    */
  def getAllPlayersForSeason(season: Short): List[Player] =
    service.getAllPlayersForSeason(season)

  /** Get the actual in game performance for a player
    *
    * @param player the player we want to find the performance for
    * @param context the context in which to find the performance
    *
    * @throws NoPerformanceException if no in game performance is found
    */
  @throws(classOf[NoPerformanceException])
  def getFPLPerformance(player: Player, context: Context): FPLPlayerGameweek = {
    val perf = service.getFPLPerformance(player, context)
    if (perf.size == 1) perf.head
    else {
      // The only possibility here is that there are no performances,
      // due to the database schema preventing there being multiple
      // performances with the same player and gameweek IDs
      val playerId = player.id
      val season = context.gameweek.season
      val gameweekNumber = context.gameweek.gameweek
      throw new NoPerformanceException(
        s"No performance for player #$playerId - $season GW$gameweekNumber"
      )
    }
  }

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

  /** Get the relevant set of fixtures for the player in the given
    * context i.e. the set of fixtures which we are allowed to use
    * in the prediction
    *
    * @param player the player we're interested in
    * @param context the context to compare fixtures against
    */
  def getRelevantFixtures(
    player: Player,
    context: Context
  ): List[(PlayerFixture, Fixture)] = {
    val allFixtures = service.getAllFixturesForPlayer(player)
    val deadline = context.gameweek.deadline.toLocalDate
    allFixtures
      .filter({
        case (_, fixture) => fixture.hasDate
      })
      .filter({
        case (_, fixture) =>
          val date = fixture.fixtureDate.get
          date.compareTo(deadline) < 0
      })
  }
}

/** An exception indicating that no relevant FPL performance was found
  * for the given query.
  *
  * @param message the message
  * @param cause the cause, defaulting to None
  */
final class NoPerformanceException(
  message: String,
  cause: Throwable = None.orNull
) extends Exception(message, cause)
