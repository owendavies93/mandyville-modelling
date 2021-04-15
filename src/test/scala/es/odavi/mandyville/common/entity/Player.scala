package es.odavi.mandyville.common.entity

import io.getquill.{PostgresDialect, SnakeCase, SqlMirrorContext}
import org.scalatest.funsuite.AnyFunSuite

class PlayerSuite extends AnyFunSuite {

  val ctx = new SqlMirrorContext(PostgresDialect, SnakeCase)
  import ctx._

  // TODO: move these table name overrides into a shared object
  val players = quote {
    querySchema[Player]("players")
  }

  test("Generates correct query for simple player fetch") {
    assertResult("SELECT p.fpl_id FROM players p WHERE p.first_name = 'Owen'") {
      val q = quote {
        players
          .filter(p => p.firstName == "Owen")
          .map(p => p.fplId)
      }

      ctx.run(q).string
    }
  }
}
