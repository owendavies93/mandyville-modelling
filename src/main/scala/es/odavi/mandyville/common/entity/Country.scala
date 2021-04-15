package es.odavi.mandyville.common.entity

/** A country of the world
  *
  * @constructor create a country with a name and a code
  * @param name the country name
  * @param code the ISO Alpha-2 code for the country
  */
case class Country(id: Int, name: String, code: String)

/** An alternate name for a country
  *
  * @constructor create a country alternative name with a country ID and a name
  * @param country_id the ID of the country in the countries table
  * @param alternate_name the alternate name of the country
  */
case class CountryAlternateName(
  id: Int,
  countryId: Int,
  alternateName: String
)
