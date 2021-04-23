/*
    mandyville modelling - build.sbt
 */

ThisBuild / scalaVersion := "2.13.0"
ThisBuild / organization := "es.odavi.mandyville"

val testVersion = "3.2.7"
val mockVersion = "1.16.37"
val scalaTic = "org.scalactic"  %% "scalactic"     % testVersion
val scalaTest = "org.scalatest" %% "scalatest"     % testVersion
val mockito = "org.mockito"     %% "mockito-scala" % mockVersion

val quillVersion = "3.7.0"

val quill = Seq(
  "io.getquill" %% "quill-async-postgres",
  "io.getquill" %% "quill-jdbc"
).map(_ % quillVersion)

val pgVersion = "42.2.19"
val pg = "org.postgresql" % "postgresql" % pgVersion

val dIVersion = "0.9.9"
val dockerIt = Seq(
  "com.whisk" %% "docker-testkit-scalatest",
  "com.whisk" %% "docker-testkit-impl-docker-java"
).map(_ % dIVersion)

val flywayVersion = "7.8.1"
val flyway = "org.flywaydb" % "flyway-core" % flywayVersion

lazy val settings = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "Mandyville Modelling",
    Defaults.itSettings,
    libraryDependencies += scalaTic,
    libraryDependencies += scalaTest % "it,test",
    libraryDependencies += mockito   % Test,
    libraryDependencies ++= quill,
    libraryDependencies += pg,
    libraryDependencies ++= dockerIt,
    libraryDependencies += flyway,
    scalacOptions ++= Seq("-deprecation", "-feature"),
  )
