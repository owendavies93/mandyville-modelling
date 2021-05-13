package es.odavi.mandyville

import common.entity.Player
import common.Comparison

import scala.math.BigDecimal.RoundingMode

/** The base predictor providing shared predictor functionality
  * and an interface for all predictors.
  *
  * @constructor create a new predictor for a player in a context
  * @param player the player for whom we want to predict
  * @param context the context in which we are predicting
  * @param playerManager an instance of PlayerManager
  */
abstract class Predictor(
  player: Player,
  context: Context,
  playerManager: PlayerManager
) {

  /** Compare a prediction with the real life result
    *
    * @return an instance of Comparison representing our comparison
    */
  def comparePrediction(): Comparison = {
    val prediction = pointsForGameweek()
    val perf = playerManager.getFPLPerformance(player, context)
    val actual = perf.totalPoints
    Comparison(player, context, prediction, BigDecimal(actual))
  }

  /** The unique ID of this predictor, used primarily when saving
    * results.
    */
  def id: Int

  /** Find the predicted points for the gameweek provided in the
    * given context.
    *
    * Fetch the expected values and probabilities of various in-game
    * events, and combine them with the FPL ruleset to generate a
    * points total.
    *
    * @return the total predicted points
    */
  def pointsForGameweek(): BigDecimal = {
    val mins = expectedMinutes()

    if (mins == 0) 0
    else {
      val position = playerManager.getPositionForPlayer(player, context)

      val goalPoints = expectedGoals() * GoalPoints(position)
      val assistPoints = expectedAssists() * AssistPoints

      val conceded = expectedConceded()

      // TODO: do we need to think about whether the player is actually on the
      //       pitch for the conceded goals here?
      val cleanSheetPoints =
        if (conceded < 0.5 && mins > 60) BigDecimal(CleanSheetPoints(position))
        else if (conceded >= 1.5 && mins > 60) {
          val lastEven = (conceded / 2).setScale(0, RoundingMode.FLOOR)
          lastEven * TwoGoalsConcededPoints
        } else BigDecimal(0)

      val redPenalty = chanceOfRedCard() * RedCardPoints
      val yellowPenalty = chanceOfYellowCard() * YellowCardPoints

      val appearancePoints = if (mins >= 60) FullPlayPoints else SubPlayPoints

      // TODO:
      // * Penalty Saves
      // * Penalty Misses
      // * Own Goals
      // * Bonus Points

      goalPoints + assistPoints + cleanSheetPoints + redPenalty +
        yellowPenalty + appearancePoints
    }
  }

  /** Find the probability of the player getting a red card in the
    * gameweek given by the context.
    */
  def chanceOfRedCard(): BigDecimal

  /** Find the probability of the player getting a yellow card in the
    * gameweek given by the context.
    */
  def chanceOfYellowCard(): BigDecimal

  /** Find the expected number of assists for the player
    */
  def expectedAssists(): BigDecimal

  /** Find the expected number of goals conceded by the player's team
    * in the gameweek given by the context.
    */
  def expectedConceded(): BigDecimal

  /** Find the expected number of goals scored by the player
    */
  def expectedGoals(): BigDecimal

  /** Find the expected number of minutes played by the player in the
    * gameweek given by the context
    */
  def expectedMinutes(): BigDecimal
}
