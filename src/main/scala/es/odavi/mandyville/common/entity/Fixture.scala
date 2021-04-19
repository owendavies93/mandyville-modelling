package es.odavi.mandyville.common.entity

import org.joda.time.LocalDate

import scala.math.BigDecimal

/** A football fixture
  *
  * @constructor create a new fixture with:
  * @param competitionId the compeition that the fixture is in
  * @param homeTeamId the ID of the home team
  * @param awayTeamId the ID of the away team
  * @param season the year of the season of the fixture
  * @param winningTeamId the ID of the winning team (if applicable)
  * @param homeTeamGoals the number of goals scored by the home goals
  * @param awayTeamGoals the number of goals scored by the away goals
  * @param fixtureDate the date of the fixture
  */
case class Fixture(
  id: Int,
  competitionId: Int,
  homeTeamId: Int,
  awayTeamId: Int,
  season: Short,
  winningTeamId: Option[Int],
  homeTeamGoals: Option[Short],
  awayTeamGoals: Option[Short],
  fixtureDate: Option[LocalDate]
)

/** An enumeration defining the categories of position that a player
  * can play in
  */
object PositionCategory extends Enumeration {
  type PositionCategory = Value

  val Goalkeeper, Defender, Midfielder, Forward = Value
}

import PositionCategory._

/** A position on the football pitch
  *
  * @constructor create a new position with a name and category
  * @param name the name of the position
  * @param positionCategory the category of the position
  */
case class Position(
  id: Int,
  name: String,
  positionCategory: PositionCategory
)

/** A player's performance in a fixture
  *
  * Half of the attributes are optional because this class encapsulates
  * both fixtures in the past and fixtures in the future.
  *
  * @constructor create a new PlayerFixture with:
  * @param playerId the player's ID
  * @param fixtureId the ID of the fixture
  * @param teamId the ID of the team the player played for
  * @param minutes the number of minutes they played for
  * @param yellowCard whether the player recieved a yellow card
  * @param redCard whether the player recieved a red card
  * @param goals the number of goals the player scored
  * @param assists the number of assists the player achieved
  * @param keyPasses the number of 'key passes' the player made
  * @param xg the expected goals of the player
  * @param xa the expected assists of the player
  * @param xgBuildup the xgBuildup of the player
  * @param xgChain the xgChain of the player
  * @param postitionId the position of the player
  * @param npg the non-penalty goals scored by the player
  * @param npxg the expected non-penalty goals scored by the player
  */
case class PlayerFixture(
  id: Int,
  playerId: Int,
  fixtureId: Int,
  teamId: Int,
  minutes: Short,
  yellowCard: Boolean,
  redCard: Boolean,
  goals: Option[Short],
  assists: Option[Short],
  keyPasses: Option[Short],
  xg: Option[BigDecimal],
  xa: Option[BigDecimal],
  xgBuildup: Option[BigDecimal],
  xgChain: Option[BigDecimal],
  positionId: Option[Int],
  npg: Option[Short],
  npxg: Option[BigDecimal]
)
