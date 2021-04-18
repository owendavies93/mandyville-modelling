package es.odavi.mandyville.common.entity

import com.github.nscala_time.time.Imports.DateTime

/** A gamweek in the Fantasy Premier League game
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
