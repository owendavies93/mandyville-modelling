package es.odavi.mandyville.common.entity

import es.odavi.mandyville.common.Schema

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class FPLGameweekSuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext[PostgresDialect, SnakeCase](
    PostgresDialect,
    SnakeCase
  ) with Schema
  import ctx._

  test("Generates correct query for simple gameweek fetch") {
    assertResult(
      "SELECT f.id FROM fpl_gameweeks f WHERE f.season = 2020 AND f.gameweek = 1"
    ) {
      val q = quote {
        fplGameweeks
          .filter(f => f.season == 2020 && f.gameweek == 1)
          .map(f => f.id)
      }

      ctx.run(q).string
    }
  }

  test("Generates correct joins for player season info query") {
    val q = quote {
      players
        .join(fplSeasonInfo)
        .on(_.id == _.playerId)
        .filter { case (p, f) => p.id == 1 }
        .map { case (p, f) => f.fplSeasonId }
    }

    val queryString = ctx.run(q).string

    assert(queryString.contains("SELECT x2.fpl_season_id"))
    assert(queryString.contains("JOIN fpl_season_info x2"))
  }
}
