package es.odavi.mandyville.common

import entity._
import io.getquill.{EntityQuery, PostgresDialect, SnakeCase}
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
  */
trait Schema { this: Context[PostgresDialect, SnakeCase] =>

  def competitions: Quoted[EntityQuery[Competition]] =
    quote {
      querySchema[Competition]("competitions")
    }

  def countries: Quoted[EntityQuery[Country]] =
    quote {
      querySchema[Country]("countries")
    }

  def countryAlternateNames: Quoted[EntityQuery[CountryAlternateName]] =
    quote {
      querySchema[CountryAlternateName]("country_alternate_names")
    }

  def fixtures: Quoted[EntityQuery[Fixture]] =
    quote {
      querySchema[Fixture]("fixtures")
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

  def playersFixtures: Quoted[EntityQuery[PlayerFixture]] =
    quote {
      querySchema[PlayerFixture]("players_fixtures")
    }

  def teams: Quoted[EntityQuery[Team]] =
    quote {
      querySchema[Team]("teams")
    }

  def teamAlternateNames: Quoted[EntityQuery[TeamAlternateName]] =
    quote {
      querySchema[TeamAlternateName]("team_alternate_names")
    }
}
