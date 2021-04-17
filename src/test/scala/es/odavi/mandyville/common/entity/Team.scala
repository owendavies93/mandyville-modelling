package es.odavi.mandyville.common.entity

import es.odavi.mandyville.common.Schema

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class TeamSuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext[PostgresDialect, SnakeCase](
    PostgresDialect,
    SnakeCase
  ) with Schema
  import ctx._

  test("Generates correct query for simple team fetch") {
    assertResult(
      "SELECT t.football_data_id FROM teams t WHERE t.name = 'Chelsea FC'"
    ) {
      val q = quote {
        teams
          .filter(t => t.name == "Chelsea FC")
          .map(t => t.footballDataId)
      }

      ctx.run(q).string
    }
  }

  test("Generates correct query for alternate name fetch") {
    assertResult(
      "SELECT x1.id FROM teams x1 INNER JOIN team_alternate_names x2 ON x1.id = x2.team_id WHERE x2.name = 'Chelsea'"
    ) {
      val q = quote {
        teams
          .join(teamAlternateNames)
          .on(_.id == _.teamId)
          .filter { case (t, a) => a.name == "Chelsea" }
          .map { case (t, a) => t.id }
      }

      ctx.run(q).string
    }
  }
}
