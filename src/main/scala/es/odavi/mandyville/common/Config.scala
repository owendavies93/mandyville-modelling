package es.odavi.mandyville.common

import com.typesafe.config._

/** Factory object for mandyville.common.config instances */
object Config {

  /** Returns the current environment we're in
    *
    * If the MANDYVILLE_ENV environemnt variable is defined, use that.
    * Else, returns 'development'
    */
  def env = sys.env.getOrElse("MANDYVILLE_ENV", "development")

  /** Creates an instance of the config */
  def apply(): Config = {
    val config = ConfigFactory.load(env)
    config.getConfig("mandyville")
  }
}
