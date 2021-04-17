package es.odavi.mandyville.common.entity

/** A football team
  *
  * @constructor create a team with a name and a football-data ID
  * @param name the team name
  * @param name the football-data ID for the team
  */
case class Team(id: Int, name: String, footballDataId: Int)

/** An alternate name for a team
  *
  * @constructor create a team alternate name with a team ID and a name
  * @param team_id the ID of the team in the teams table
  * @param name the alternate name of the team
  */
case class TeamAlternateName(id: Int, teamId: Int, name: String)
