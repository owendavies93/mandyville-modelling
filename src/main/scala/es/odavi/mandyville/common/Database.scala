package es.odavi.mandyville.common

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import com.whisk.docker._
import org.postgresql.ds.PGSimpleDataSource
import org.flywaydb.core.Flyway
import io.getquill.{PostgresJdbcContext, SnakeCase}

import java.sql.DriverManager
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/** Provides accessors for the database config, both in the form of
  * the standard typesafe Config object and the HikariConfig
  */
trait DatabaseConfig {

  /** The typesafe Config
    */
  def config = Config()

  private val pgDataSource = new PGSimpleDataSource()

  pgDataSource.setUser(config.getString("database.user"))
  pgDataSource.setPassword(config.getString("database.password"))
  pgDataSource.setDatabaseName(config.getString("database.database"))
  pgDataSource.setServerNames(Array[String](config.getString("database.host")))
  pgDataSource.setPortNumbers(Array[Int](config.getInt("database.port")))

  /** The HikariConfig object with the data source set to the
    * PGSimpleDataSource
    */
  def dbConfig: HikariConfig = {
    val conf = new HikariConfig()
    conf.setDataSource(pgDataSource)
    conf
  }
}

/** Provides the context for running queries against the mandyville
  * database.
  *
  * Generally, you'll need to import Database.ctx and Database.ctx._
  * to contruct queries and the run queries with ctx.run()
  */
object Database extends DatabaseConfig {
  // These connection options are too small scale for production use,
  // I expect. But the machines currently being used in the alpha are
  // very low powered and these options make things way more stable.
  dbConfig.setMaximumPoolSize(10)
  dbConfig.setMinimumIdle(1)
  dbConfig.setIdleTimeout(1000)

  private val dbSource = new HikariDataSource(dbConfig)
  val ctx = new PostgresJdbcContext[SnakeCase](SnakeCase, dbSource) with Schema
}

/** An implementation of DockerReadyChecker, used to wait for the
  * postgres docker container to spin up. Cribbed from the
  * docker-it-scala examples.
  *
  * @param url the DB url
  * @param user the DB user
  * @param password the DB password
  * @param driver the DB driver class, as a String
  */
class PostgresReadyChecker(
  url: String,
  user: String,
  password: String,
  driver: String
) extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(implicit
    docker: DockerCommandExecutor,
    ec: ExecutionContext
  ): Future[Boolean] =
    container
      .getPorts()
      .map(_ =>
        Try {
          Class.forName(driver)
          Option(DriverManager.getConnection(url, user, password))
            .map(_.close)
            .isDefined
        }.getOrElse(false)
      )
}

/** A trait to create a docker container that contains a postgres
  * database. Builds the docker container and adds it to the list of
  * containers that DockerKit will pull in.
  */
trait TestPostgresDatabase extends DockerKit with DatabaseConfig {
  def InternalPort: Int = 4444

  val externalPort: Int = config.getInt("database.port")
  val user: String = config.getString("database.user")
  val password: String = config.getString("database.password")
  val database: String = config.getString("database.database")

  val dbUrl =
    s"jdbc:postgresql://localhost:$externalPort/$database?autoReconnect=true&useSSL=false"
  val driver: String = "org.postgresql.Driver"
  val dockerImage = "postgres:13.2"

  val postgresContainer: DockerContainer = DockerContainer(dockerImage)
    .withPorts((externalPort, Some(InternalPort)))
    .withEnv(
      s"POSTGRES_USER=$user",
      s"POSTGRES_PASSWORD=$password",
      s"POSTGRES_DB=$database"
    )
    .withReadyChecker(
      new PostgresReadyChecker(dbUrl, user, password, driver)
    )

  /** The list of docker containers from the parent class, plus our new
    * postgres container.
    */
  abstract override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers
}

/** A trait that defines the configuration for Flyway migrations, to be
  * used in integration tests.
  */
trait FlywayConfig {

  /** Define the configuration and set the various options required to
    * load migration files from the submodule defined the the resources
    * folder.
    *
    * @return an instance of Flyway
    */
  def flyway: Flyway =
    Flyway
      .configure()
      .sqlMigrationPrefix("")
      .sqlMigrationSeparator("_")
      .locations("meta")
      .load()
}
