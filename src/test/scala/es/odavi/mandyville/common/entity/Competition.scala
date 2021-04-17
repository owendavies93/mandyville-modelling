package es.odavi.mandyville.common.entity

import es.odavi.mandyville.common.Schema

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class CompetitionSuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext[PostgresDialect, SnakeCase](
    PostgresDialect,
    SnakeCase
  ) with Schema
  import ctx._

  test("Generates correct query for simple competition fetch") {
    assertResult("SELECT c.football_data_plan FROM competitions c") {
      val q = quote {
        competitions.map(c => c.footballDataPlan)
      }

      ctx.run(q).string
    }
  }
}
