package es.odavi.mandyville.common.entity

import es.odavi.mandyville.common.Schema

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class CountrySuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext[PostgresDialect, SnakeCase](
    PostgresDialect,
    SnakeCase
  ) with Schema
  import ctx._

  test("Generates correct query for simple country fetch") {
    assertResult("SELECT c.id FROM countries c WHERE c.name = 'Afghanistan'") {
      val q = quote {
        countries
          .filter(c => c.name == "Afghanistan")
          .map(c => c.id)
      }

      ctx.run(q).string
    }
  }

  test("Generates correct query for alternate name fetch") {
    assertResult(
      "SELECT x1.id FROM countries x1 INNER JOIN country_alternate_names x2 ON x1.id = x2.country_id WHERE x2.alternate_name = 'United States'"
    ) {
      val q = quote {
        countries
          .join(countryAlternateNames)
          .on(_.id == _.countryId)
          .filter { case (c, a) => a.alternateName == "United States" }
          .map { case (c, a) => c.id }
      }

      ctx.run(q).string
    }
  }
}
