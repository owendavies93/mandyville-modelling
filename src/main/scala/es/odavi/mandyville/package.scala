package es.odavi

import mandyville.common.entity.PositionCategory
import mandyville.common.entity.PositionCategory._

/** Provides classes for predicting player and team performance in
  * football matches. It includes:
  *
  * <p><ul>
  * <li> predictive models, based on the Model abstract class
  * <li> encapsulation of the FPL rules
  * <li> prediction Context, defining the data that is permitted to be
  *      used in the prediction and the point in time for which we are
  *      predicting
  * </ul></p>
  */
package object mandyville {
  // Point values for FPL player events
  val AssistPoints = 3

  val CleanSheetPoints = Map[PositionCategory.Value, Int](
    Goalkeeper -> 4,
    Defender -> 4,
    Midfielder -> 1,
    Forward -> 0
  )

  val FullPlayPoints = 2

  val GoalPoints = Map[PositionCategory.Value, Int](
    Goalkeeper -> 6,
    Defender -> 6,
    Midfielder -> 5,
    Forward -> 4
  )

  val OwnGoalPoints = -2

  val PenaltyMissPoints = -2

  val PenaltySavePoints = 5

  val RedCardPoints = -3

  val SubPlayPoints = 1

  val TwoGoalsConcededPoints = -1

  val YellowCardPoints = -1

  // Other useful constant values
  val DecimalPlaces = 3
}
