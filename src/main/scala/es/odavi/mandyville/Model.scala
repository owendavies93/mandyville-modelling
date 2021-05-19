package es.odavi.mandyville

import es.odavi.mandyville.common.Comparison
import es.odavi.mandyville.common.entity.{Player, Prediction}

/** This class provides methods for aggregating predictions outputted
  * by a predictor, and performing analysis on those predictions. It
  * operates over a fixed context but can be configured to run using an
  * arbitrary predictor.
  *
  * @param context the context in which we want to predict
  * @param playerManager an instance of PlayerManager, mostly used for
  *                      database interaction
  */
class Model(
  context: Context,
  playerManager: PlayerManager,
  predictionManager: PredictionManager
) {

  /** Calculate the correlation of the predicted results and the actual
    * real life scores.
    *
    * @param comparisons the set of comparisons, as produced by
    *                    runPredictions
    * @return the correlation score between 1 and -1
    */
  def correlatePredictions(comparisons: Set[Comparison]): BigDecimal = {
    val predictions = comparisons.toSeq.map(c => c.expected)
    val results = comparisons.toSeq.map(c => c.actual)

    val mP = mean(predictions)
    val mR = mean(results)

    val sum = comparisons.map(c => (c.expected - mP) * (c.actual - mR)).sum

    val covariance = sum / (comparisons.size - 1)

    if (covariance == 0) 0
    else
      covariance / (stdDev(predictions) * stdDev(results))
  }

  /** Run predictions for all active players in a given context - that
    * is, any player that is registered in the FPL game in the season
    * given by the context.
    *
    * @param factory a function that creates a new predictor
    * @tparam P a class extended from Predictor
    * @return a set of Comparison objects
    */
  def runPredictions[P <: Predictor](
    factory: (Context, PlayerManager) => P
  ): Set[Comparison] = {
    val players = playerManager.getAllPlayersForSeason(context.gameweek.season)
    val predictor = factory(context, playerManager)

    players
      .map(p => predictor.comparePrediction(p))
      .toSet
  }

  private def mean[T](seq: Iterable[T])(implicit T: Fractional[T]): T =
    T.div(seq.sum, T.fromInt(seq.size))

  private def variance[T](
    seq: Iterable[T]
  )(implicit T: Fractional[T]): Double = {
    val m = mean(seq)
    seq.map(t => math.pow(T.toDouble(T.minus(t, m)), 2)).sum / seq.size
  }

  private def stdDev[T](set: Iterable[T])(implicit T: Fractional[T]): Double =
    math.sqrt(variance(set))

}
