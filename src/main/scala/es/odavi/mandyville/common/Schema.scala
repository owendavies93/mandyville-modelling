package es.odavi.mandyville.common

import entity._
import io.getquill.{
  ActionReturning,
  EntityQuery,
  Insert,
  PostgresDialect,
  SnakeCase
}
import io.getquill.context.Context

/** Provides mappings for custom identifiers to tables in tests
  * database. This is required because the database table names are
  * pluralised, whereas the case classes defined in the
  * mandyville.common.entity package are singular.
  *
  * This trait can be used as a mixin with any Context. The Idiom is
  * fixed to PostgresDialect and the NamingConvention is fixed to
  * SnakeCase. This ensures that queries produced by mirror contexts
  * in tests are reflective of queries that are passed to the actual
  * database.
  *
  * This trait also provides decoders and encoders for custom types
  * used in the mandyville database.
  *
  * This trait also provides methods for inserting entities into
  * some collections. This is primarily for use in testing.
  */
trait Schema { this: Context[PostgresDialect, SnakeCase] =>

  def competitions: Quoted[EntityQuery[Competition]] =
    quote {
      querySchema[Competition]("competitions")
    }

  def insertCompetition(c: Competition): Quoted[Insert[Competition]] =
    quote {
      competitions.insert(
        _.name -> lift(c.name),
        _.countryId -> lift(c.countryId)
        /*
        _.footballDataId -> lift(c.footballDataId),
        _.footballDataPlan -> lift(c.footballDataPlan),
         */
      )
    }

  def countries: Quoted[EntityQuery[Country]] =
    quote {
      querySchema[Country]("countries")
    }

  def insertCountry(c: Country): Quoted[Insert[Country]] =
    quote {
      countries.insert(
        _.name -> lift(c.name),
        _.code -> lift(c.code)
      )
    }

  def countryAlternateNames: Quoted[EntityQuery[CountryAlternateName]] =
    quote {
      querySchema[CountryAlternateName]("country_alternate_names")
    }

  def fixtures: Quoted[EntityQuery[Fixture]] =
    quote {
      querySchema[Fixture]("fixtures")
    }

  def insertFixture(f: Fixture): Quoted[ActionReturning[Fixture, Index]] =
    quote {
      fixtures
        .insert(
          _.competitionId -> lift(f.competitionId),
          _.homeTeamId -> lift(f.homeTeamId),
          _.awayTeamId -> lift(f.awayTeamId),
          _.season -> lift(f.season)
          /*
        _.winningTeamId -> f.winningTeamId,
        _.homeTeamGoals -> f.homeTeamGoals,
        _.awayTeamGoals -> f.awayTeamGoals,
        _.fixtureDate -> f.fixtureDate,
           */
        )
        .returningGenerated(_.id)
    }

  def fplGameweeks: Quoted[EntityQuery[FPLGameweek]] =
    quote {
      querySchema[FPLGameweek]("fpl_gameweeks")
    }

  def fplPlayersGameweeks: Quoted[EntityQuery[FPLPlayerGameweek]] =
    quote {
      querySchema[FPLPlayerGameweek]("fpl_players_gameweeks")
    }

  def fplPositions: Quoted[EntityQuery[FPLPosition]] =
    quote {
      querySchema[FPLPosition]("fpl_positions")
    }

  def fplSeasonInfo: Quoted[EntityQuery[FPLSeasonInfo]] =
    quote {
      querySchema[FPLSeasonInfo]("fpl_season_info")
    }

  def positions: Quoted[EntityQuery[Position]] =
    quote {
      querySchema[Position]("positions")
    }

  def players: Quoted[EntityQuery[Player]] =
    quote {
      querySchema[Player]("players")
    }

  def insertPlayer(p: Player): Quoted[Insert[Player]] =
    quote {
      players.insert(
        _.firstName -> lift(p.firstName),
        _.lastName -> lift(p.lastName),
        _.countryId -> lift(p.countryId)
        /*
        _.footballDataId -> p.footballDataId,
        _.understatId -> p.understatId,
        _.fplId -> p.fplId,
         */
      )
    }

  def playersFixtures: Quoted[EntityQuery[PlayerFixture]] =
    quote {
      querySchema[PlayerFixture]("players_fixtures")
    }

  def insertPlayerFixture(pf: PlayerFixture): Quoted[Insert[PlayerFixture]] =
    quote {
      playersFixtures.insert(
        _.playerId -> lift(pf.playerId),
        _.fixtureId -> lift(pf.fixtureId),
        _.teamId -> lift(pf.teamId),
        _.minutes -> lift(pf.minutes),
        _.yellowCard -> lift(pf.yellowCard),
        _.redCard -> lift(pf.redCard)
        /*
        _.goals -> pf.goals,
        _.assists -> pf.assists,
        _.keyPasses -> pf.keyPasses,
        _.xg -> pf.xg,
        _.xa -> pf.xa,
        _.xgBuildup-> pf.xgBuildup,
        _.xgChain-> pf.xgChain,
        _.positionId -> pf.positionId,
        _.npg -> pf.npg,
        _.npxg -> pf.npxg
         */
      )
    }

  def teams: Quoted[EntityQuery[Team]] =
    quote {
      querySchema[Team]("teams")
    }

  def insertTeam(t: Team): Quoted[Insert[Team]] =
    quote {
      teams.insert(
        _.name -> lift(t.name),
        _.footballDataId -> lift(t.footballDataId)
      )
    }

  def teamAlternateNames: Quoted[EntityQuery[TeamAlternateName]] =
    quote {
      querySchema[TeamAlternateName]("team_alternate_names")
    }
}
