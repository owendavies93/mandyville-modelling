package es.odavi.mandyville.common.entity

import es.odavi.mandyville.common.Schema

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class FixtureSuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext[PostgresDialect, SnakeCase](
    PostgresDialect,
    SnakeCase
  ) with Schema
  import ctx._

  test("Joins correctly for fixture fetch") {
    val q = quote {
      for {
        f <- fixtures
        c <- competitions.join(c => f.competitionId == c.id)
        h <- teams.join(h => f.homeTeamId == h.id)
        a <- teams.join(a => f.awayTeamId == a.id)
      } yield (f, c, h, a)
    }

    val queryString = ctx.run(q).string

    assert(queryString.contains("FROM fixtures f"))
    assert(queryString.contains("INNER JOIN competitions c"))
    assert(queryString.contains("INNER JOIN teams h"))
    assert(queryString.contains("INNER JOIN teams a"))
  }

  test("Joins correctly for player fixture fetch") {
    val q = quote {
      for {
        pf <- playersFixtures
        p <- players.join(p => pf.playerId == p.id)
        f <- fixtures.join(f => pf.fixtureId == f.id)
        t <- teams.join(t => pf.teamId == t.id)
      } yield (pf, p, f, t)
    }

    val queryString = ctx.run(q).string
    assert(queryString.contains("FROM players_fixtures pf"))
    assert(queryString.contains("INNER JOIN players p"))
    assert(queryString.contains("INNER JOIN fixtures f"))
    assert(queryString.contains("INNER JOIN teams t"))
  }
}
