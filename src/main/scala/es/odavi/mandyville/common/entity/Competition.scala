package es.odavi.mandyville.common.entity

/** A football competition
  *
  * @constructor create a competition with a name and a country ID
  * @param name the competition name
  * @param countryId the country ID
  * @param footballDataId the football-data ID of the competition
  * @param footballDataPlan the football-data plan tier that the
  *   competition belongs to
  */
case class Competition(
  id: Int,
  name: String,
  countryId: Int,
  footballDataId: Option[Int],
  footballDataPlan: Option[Int]
)
