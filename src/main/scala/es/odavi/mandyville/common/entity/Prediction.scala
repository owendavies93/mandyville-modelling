package es.odavi.mandyville.common.entity

/** A prediction - the output of a predictor
  *
  * @param playerId the ID of the player
  * @param fplGameweekId the ID of the gameweek for the prediction
  * @param predictorId the ID of the predictor used to predict
  * @param prediction the prediction
  */
case class Prediction(
  id: Int,
  playerId: Int,
  fplGameweekId: Int,
  predictorId: Int,
  prediction: BigDecimal
)
