package es.odavi.mandyville.common

import entity._
import es.odavi.mandyville.common.entity.PositionCategory.PositionCategory
import io.getquill.{
  ActionReturning,
  EntityQuery,
  Insert,
  PostgresDialect,
  SnakeCase
}
import io.getquill.context.Context
import io.getquill.context.jdbc.JdbcContext

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

/** This trait provides methods for inserting entities into
  * some collections. This is primarily for use in testing.
  *
  * This trait also provides decoders and encoders for custom types
  * used in the mandyville database.
  */
trait InsertSchema extends Schema {
  this: JdbcContext[PostgresDialect, SnakeCase] =>

  implicit val competitionInsertMeta = insertMeta[Competition](_.id)

  def insertCompetition(c: Competition): Quoted[Insert[Competition]] =
    quote {
      competitions.insert(lift(c))
    }

  implicit val countryInsertMeta = insertMeta[Country](_.id)

  def insertCountry(c: Country): Quoted[Insert[Country]] =
    quote {
      countries.insert(lift(c))
    }

  implicit val fixtureInsertMeta = insertMeta[Fixture](_.id)

  def insertFixture(f: Fixture): Quoted[ActionReturning[Fixture, Index]] =
    quote {
      fixtures.insert(lift(f)).returningGenerated(_.id)
    }

  implicit val fPLGameweekInsertMeta = insertMeta[FPLGameweek](_.id)

  def insertFPLGameweek(
    f: FPLGameweek
  ): Quoted[ActionReturning[FPLGameweek, Index]] =
    quote {
      fplGameweeks.insert(lift(f)).returningGenerated(_.id)
    }

  implicit val fplSeasonInfoInsertMeta = insertMeta[FPLSeasonInfo](_.id)

  def insertFPLSeasonInfo(f: FPLSeasonInfo): Quoted[Insert[FPLSeasonInfo]] =
    quote {
      fplSeasonInfo.insert(lift(f))
    }

  implicit val fplPositionInsertMeta = insertMeta[FPLPosition](_.id)

  implicit val positionCategoryDecoder: Decoder[PositionCategory] =
    decoder((index, row) =>
      PositionCategory.withName(row.getObject(index).toString)
    )

  implicit val positionCategoryEncoder: Encoder[PositionCategory] =
    encoder(
      java.sql.Types.VARCHAR,
      (index, value, row) => row.setObject(index, value, java.sql.Types.OTHER)
    )

  def insertFPLPosition(f: FPLPosition): Quoted[Insert[FPLPosition]] =
    quote {
      fplPositions.insert(lift(f))
    }

  implicit val fPLPlayerGameweekInsertMeta = insertMeta[FPLPlayerGameweek](_.id)

  def insertFPLPlayerGameweek(
    f: FPLPlayerGameweek
  ): Quoted[Insert[FPLPlayerGameweek]] =
    quote {
      fplPlayersGameweeks.insert(lift(f))
    }

  implicit val playerInsertMeta = insertMeta[Player](_.id)

  def insertPlayer(p: Player): Quoted[Insert[Player]] =
    quote {
      players.insert(lift(p))
    }

  implicit val playerFixtureInsertMeta = insertMeta[PlayerFixture](_.id)

  def insertPlayerFixture(pf: PlayerFixture): Quoted[Insert[PlayerFixture]] =
    quote {
      playersFixtures.insert(lift(pf))
    }

  implicit val teamInsertMeta = insertMeta[Team](_.id)

  def insertTeam(t: Team): Quoted[Insert[Team]] =
    quote {
      teams.insert(lift(t))
    }
}
