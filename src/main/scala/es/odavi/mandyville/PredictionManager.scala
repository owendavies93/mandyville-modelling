package es.odavi.mandyville

import es.odavi.mandyville.common.{Database, InsertSchema}
import es.odavi.mandyville.common.entity.Prediction
import io.getquill.{PostgresJdbcContext, SnakeCase}

/** A generic interface for interacting with a quill context for
  * prediction related tasks
  */
trait PredictionDatabaseService {

  /** Insert the given prediction into the mandyville database, or
    * update the prediction if a matching prediction already exists.
    *
    * @param p the Prediction to store
    */
  def insertOrUpdatePrediction(p: Prediction): Int
}

/** An implementation of PredictionDatabaseService using a quill
  * context to interface with the mandyville database.
  */
class PredictionDatabaseImp(
  ctx: PostgresJdbcContext[SnakeCase] with InsertSchema = Database.ctx
) extends PredictionDatabaseService {

  import ctx._

  override def insertOrUpdatePrediction(p: Prediction): Int = {
    val q = quote {
      predictions
        .insert(lift(p))
        .onConflictUpdate(_.playerId, _.fplGameweekId, _.predictorId)((t, e) =>
          t.prediction -> e.prediction
        )
    }

    ctx.run(q.returningGenerated(_.id))
  }
}

/** Provides methods for managing predictions and storing them in the
  * mandyville database
  */
class PredictionManager(
  service: PredictionDatabaseService = new PredictionDatabaseImp
) {

  /** Insert a prediction into the database. If the prediction is
    * already stored in the database (i.e. there is a prediction for
    * the same player, gameweek and predictor), update the prediction.
    *
    * @param p the prediction we want to store
    * @return the prediction we stored, including the ID set in the
    *         database
    */
  def insertOrUpdatePrediction(p: Prediction): Prediction = {
    val idFromDb = service.insertOrUpdatePrediction(p)
    p.copy(id = idFromDb)
  }
}
