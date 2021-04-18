package es.odavi.mandyville.common

import entity._

import io.getquill.{PostgresDialect, SnakeCase}
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

  def competitions =
    quote {
      querySchema[Competition]("competitions")
    }

  def countries =
    quote {
      querySchema[Country]("countries")
    }

  def countryAlternateNames =
    quote {
      querySchema[CountryAlternateName]("country_alternate_names")
    }

  def fixtures =
    quote {
      querySchema[Fixture]("fixtures")
    }

  def fplGameweeks =
    quote {
      querySchema[FPLGameweek]("fpl_gameweeks")
    }

  def fplPositions =
    quote {
      querySchema[FPLPosition]("fpl_positions")
    }

  def fplSeasonInfo =
    quote {
      querySchema[FPLSeasonInfo]("fpl_season_info")
    }

  def positions =
    quote {
      querySchema[Position]("positions")
    }

  def players =
    quote {
      querySchema[Player]("players")
    }

  def playersFixtures =
    quote {
      querySchema[PlayerFixture]("players_fixtures")
    }

  def teams =
    quote {
      querySchema[Team]("teams")
    }

  def teamAlternateNames =
    quote {
      querySchema[TeamAlternateName]("team_alternate_names")
    }
}
