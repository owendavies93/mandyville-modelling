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
  */
trait Schema { this: Context[PostgresDialect, SnakeCase] =>

  def countries =
    quote {
      querySchema[Country]("countries")
    }

  def countryAlternateNames =
    quote {
      querySchema[CountryAlternateName]("country_alternate_names")
    }

  def players =
    quote {
      querySchema[Player]("players")
    }
}
