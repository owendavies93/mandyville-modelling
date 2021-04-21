package es.odavi.mandyville.common

/** A comparison result between two values
  *
  * @param expected the value we expected to get, generally the result
  *                 of a prediction
  * @param actual the value that occurred in real life
  */
case class Comparison(expected: BigDecimal, actual: BigDecimal) {

  /** The absolute difference between the expected and actual value
    */
  def difference: BigDecimal = expected.abs - actual.abs
}
