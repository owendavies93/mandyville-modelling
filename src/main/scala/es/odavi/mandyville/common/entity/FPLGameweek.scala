package es.odavi.mandyville.common.entity

import com.github.nscala_time.time.Imports.DateTime

/** A gameweek in the Fantasy Premier League game
  *
  * @constructor create a gameweek with a season, gameweek number and
  *   deadline datetime
  * @param season the year representation of the season
  * @param gameweek the gameweek number of the season, between 1 and 38
  * @param deadline the deadline datetime of the gameweek
  */
case class FPLGameweek(
  id: Int,
  season: Short,
  gameweek: Short,
  deadline: DateTime
)

/** A player's performance in a given FPL gameweek
  *
  * @param playerId the ID of the player
  * @param fplGameweekId the ID of the FPL Gameweek
  * @param totalPoints the total FPL game points scored by the player
  * @param bonusPoints the total bonus points scored (0, 1, 2, or 3)
  * @param bps the BPS points accrued by the player
  * @param value the in-game value of the player
  * @param selected the number of people who have selected the player
  * @param transfersIn the number of people who have bought the player
  *                    this gameweek
  * @param transfersOut the number of people who have sold the player
  *                     this gamweweek
  */
case class FPLPlayerGameweek(
  id: Int,
  playerId: Int,
  fplGameweekId: Int,
  totalPoints: Short,
  bonusPoints: Short,
  bps: Short,
  value: BigDecimal,
  selected: Int,
  transfersIn: Int,
  transfersOut: Int
)

import PositionCategory._

/** A position category in the Fantasy Premier League game
  *
  * @constructor create a position category
  * @param elementTypeId the ID of the category in the game
  * @param elementType the position category associated with this ID
  */
case class FPLPosition(
  id: Int,
  elementTypeId: Int,
  elementType: PositionCategory
)

/** The season information for a player in the FPL game
  *
  * @constructor create the season info entry
  * @param playerId the ID of the player associated with this info
  * @param season the year representaton of the season
  * @param fplSeasonId the ID of the player in the season. This is not
  *   the unique FPL 'code' value, and is only unique within the FPL
  *   season - it'll be a number under 1000
  * @param fplPositionsId the position category of the player for the
  *   season in the FPL game
  */
case class FPLSeasonInfo(
  id: Int,
  playerId: Int,
  season: Short,
  fplSeasonId: Short,
  fplPositionsId: Short
)
