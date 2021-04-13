/*
    mandyville modelling - build.sbt
*/

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "es.odavi.mandyville"

val testVersion = "3.2.7"
val scalaTic  = "org.scalactic" %% "scalactic" % testVersion
val scalaTest = "org.scalatest" %% "scalatest" % testVersion

lazy val settings = (project in file("."))
    .settings(
        name := "Mandyville Modelling",
        libraryDependencies += scalaTic,
        libraryDependencies += scalaTest % Test,
    )
