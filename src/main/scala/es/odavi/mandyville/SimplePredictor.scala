package es.odavi.mandyville

import es.odavi.mandyville.common.entity.{Fixture, Player, PlayerFixture}

/** A simple predictor which essentially takes the average of all
  * relevant metrics in order to do naive predictions. A baseline for
  * comparison against other predictors.
  *
  * @param player the player for whom we want to predict
  * @param context the context in which we are predicting
  * @param playerManager an instance of PlayerManager
  */
class SimplePredictor(
  player: Player,
  context: Context,
  playerManager: PlayerManager
) extends Predictor(player, context, playerManager) {

  private var fixtures: Option[List[(PlayerFixture, Fixture)]] = None

  /** Find the probability of the player getting a red card in the
    * gameweek given by the context.
    *
    * Return the proportion of games where the player has received a
    * yellow card.
    */
  override def chanceOfRedCard(): BigDecimal =
    booleanProportion(getFixtures.map { case (p, _) => p.redCard })

  /** Find the probability of the player getting a yellow card in the
    * gameweek given by the context.
    *
    * Return the proportion of games where the player has received a
    * red card.
    */
  override def chanceOfYellowCard(): BigDecimal =
    booleanProportion(getFixtures.map { case (p, _) => p.yellowCard })

  /** Find the expected number of assists for the player, by taking the
    * average of the xa metric for all previous fixtures that the
    * player has been involved in.
    */
  override def expectedAssists(): BigDecimal = {
    val fixtures = getFixtures.filter { case (p, _) => p.xa.isDefined }
    if (fixtures.isEmpty) 0
    else
      bigDecimalAverage(fixtures.map {
        case (p, _) => p.xa.get
      })
  }

  /** Find the expected number of goals conceded by the player's team
    * in the gameweek given by the context.
    *
    * Take the average of all goals conceded by the player's team in
    * all previous fixtures. Note that you can't simply filter by a
    * single team ID here, because the player may have moved teams -
    * you need to filter by the correct team ID for each fixture.
    */
  override def expectedConceded(): BigDecimal = {
    val allGoals: List[Short] = getFixtures
      .filter {
        case (_, f) => f.hasBeenPlayed
      }
      .map {
        case (p, f) =>
          val teamID = p.teamId
          if (teamID == f.homeTeamId) f.awayTeamGoals.get
          else f.homeTeamGoals.get
      }

    BigDecimal(allGoals.sum / allGoals.size)
  }

  /** Find the expected number of goals scored by the player, by taking
    * the average of the xg metric for all previous fixtures that the
    * player has been involved in.
    */
  override def expectedGoals(): BigDecimal = {
    val fixtures = getFixtures.filter { case (p, _) => p.xg.isDefined }
    if (fixtures.isEmpty) 0
    else
      bigDecimalAverage(fixtures.map {
        case (p, _) => p.xg.get
      })
  }

  /** Find the expected number of minutes played by the player in the
    * gameweek given by the context.
    *
    * Return the average number of minutes played by the player in all
    * previous fixtures.
    */
  override def expectedMinutes(): BigDecimal =
    bigDecimalAverage(getFixtures.map { case (p, _) => p.minutes })

  private def bigDecimalAverage(l: List[BigDecimal]) =
    l.sum / BigDecimal(l.size)

  private def booleanProportion(l: List[Boolean]): BigDecimal =
    l.count(_ == true).toFloat / l.size

  // Add a caching layer to PlayerManager.getRelevantFixtures
  private def getFixtures: List[(PlayerFixture, Fixture)] = {
    if (fixtures.isEmpty)
      fixtures = Option(playerManager.getRelevantFixtures(player, context))

    fixtures.get
  }
}
