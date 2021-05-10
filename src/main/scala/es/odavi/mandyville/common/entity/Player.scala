package es.odavi.mandyville.common.entity

/** A football player
  *
  * @constructor create a player with a first and last name and a
  * country ID. The IDs for the various APIs are optional. Players
  * with names that don't conform to the first and last naming
  * scheme may have emptystring values for these names.
  * @param firstName the first name of the player
  * @param lastName the last name of the player
  * @param countryId the ID of the country the player represents
  * @param footballDataId the football-data ID for the player
  * @param understatId the understat ID for the player
  * @param fplId the FPL API Id for the player
  */
case class Player(
  id: Int,
  firstName: String,
  lastName: String,
  countryId: Int,
  footballDataId: Option[Int],
  understatId: Option[Int],
  fplId: Option[Int]
) {
  override def toString: String = s"$firstName $lastName"
}
