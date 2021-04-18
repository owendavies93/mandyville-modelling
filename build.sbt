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

val timeVersion = "2.26.0"
val nscalaTime = "com.github.nscala-time" %% "nscala-time" % timeVersion

lazy val settings = (project in file("."))
  .settings(
    name := "Mandyville Modelling",
    libraryDependencies += scalaTic,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += mockito   % Test,
    libraryDependencies ++= quill,
    libraryDependencies += pg,
    libraryDependencies += nscalaTime,
    scalacOptions ++= Seq("-deprecation", "-feature")
  )
