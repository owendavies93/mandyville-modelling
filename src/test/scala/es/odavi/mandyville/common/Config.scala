package es.odavi.mandyville.common

import org.scalatest.funsuite.AnyFunSuite

class ConfigSuite extends AnyFunSuite {

  test("Default environment is development") {
    val conf = Config()

    assertResult("0.0.0.0") {
      conf.getString("database.host")
    }
  }
}
