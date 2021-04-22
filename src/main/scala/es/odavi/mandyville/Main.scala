package es.odavi.mandyville

object Main extends App {
  val season: Short = args(0).toShort
  val gameweekNumber: Short = args(1).toShort

  /*
    - Fetch gameweek from database
    - Build context from gameweek
    - Build factory for model (maybe pass model name as argument?)
    - Run predictions
    - Correlate predictions
    - Print (and store?) results
   */
}
