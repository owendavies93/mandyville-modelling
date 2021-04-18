package es.odavi.mandyville

import common.entity.FPLGameweek

/** Provides a context for which to perform predictions
  *
  * @constructor create a new context with a gamweek
  * @param gameweek an instance of common.entity.FPLGameweek, which
  *   encapulates the information about the FPL gameweek we want to
  *   predict for.
  */
case class Context(gameweek: FPLGameweek)
