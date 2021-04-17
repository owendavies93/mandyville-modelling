package es.odavi.mandyville.common

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.postgresql.ds.PGSimpleDataSource
import io.getquill.{PostgresJdbcContext, SnakeCase}

/** Provides the context for running queries against the mandyville
  * database.
  *
  * Generally, you'll need to import Database.ctx and Database.ctx._
  * to contruct queries and the run queries with ctx.run()
  */
object Database {

  private val config = Config()
  private val pgDataSource = new PGSimpleDataSource()

  pgDataSource.setUser(config.getString("database.user"))
  pgDataSource.setPassword(config.getString("database.password"))
  pgDataSource.setDatabaseName(config.getString("database.database"))
  pgDataSource.setServerNames(Array[String](config.getString("database.host")))
  pgDataSource.setPortNumbers(Array[Int](config.getInt("database.port")))

  private val dbConfig = new HikariConfig()

  // These connection options are too small scale for production use,
  // I expect. But the machines currently being used in the alpha are
  // very low powered and these options make things way more stable.
  dbConfig.setDataSource(pgDataSource)
  dbConfig.setMaximumPoolSize(10)
  dbConfig.setMinimumIdle(1)
  dbConfig.setIdleTimeout(1000)

  private val dbSource = new HikariDataSource(dbConfig)
  val ctx = new PostgresJdbcContext[SnakeCase](SnakeCase, dbSource) with Schema
}
