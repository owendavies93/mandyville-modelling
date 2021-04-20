package es.odavi.mandyville

import es.odavi.mandyville.common.entity.{Fixture, PlayerFixture}
import org.joda.time.LocalDate

import scala.util.Random

/** Provides utility methods for testing, mostly for generating test
  * data in the form of entity case classes
  */
object TestUtils {

  /** Generates a dummy PlayerFixture and Fixture pair
    *
    * @param playerId - the ID of the player
    * @param teamId - the ID of the team the player played for
    * @param date - the date of the fixture - defaults to 1st Jan 2020
    * @param season the season - defaults to 2020
    * @param yellow force a yellow card - defaults to false
    * @param red force a red card - defaults to false
    */
  def getDummyFixtureInfo(
    playerId: Int,
    teamId: Int,
    date: Option[LocalDate] = Option(LocalDate.parse("2020-01-01")),
    season: Short = 2020,
    yellow: Boolean = false,
    red: Boolean = false
  ): (PlayerFixture, Fixture) = {
    val playerFixtureId = randomId
    val fixtureId = randomId
    val minutes = Random.between(0, 90).toShort
    (
      PlayerFixture(
        playerFixtureId,
        playerId,
        fixtureId,
        teamId,
        minutes,
        yellowCard = yellow,
        redCard = red,
        maybeShort,
        maybeShort,
        maybeShort,
        maybeBigDecimal,
        maybeBigDecimal,
        maybeBigDecimal,
        maybeBigDecimal,
        None,
        maybeShort,
        maybeBigDecimal
      ),
      Fixture(
        fixtureId,
        0,
        teamId,
        1,
        season,
        Option(teamId),
        maybeShort,
        maybeShort,
        date
      )
    )
  }

  private def maybeBigDecimal: Option[BigDecimal] =
    if (Random.nextFloat() < 0.1) None else Option(randomBigDecimal)

  private def maybeShort: Option[Short] =
    if (Random.nextFloat() < 0.1) None else Option(randomShort)

  private def randomBigDecimal: BigDecimal =
    BigDecimal(Random.between(0.0, 5.0))

  private def randomId: Int = Random.between(0, 10000)

  private def randomShort: Short = Random.between(0, 10).toShort
}
