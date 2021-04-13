package es.odavi.mandyville.common

import org.scalatest.funsuite.AnyFunSuite

class ConfigSuite extends AnyFunSuite {

  test("Default environment is development") {
    val conf = Config()

    assertResult("localhost") {
      conf.getString("database.host")
    }
  }
}
