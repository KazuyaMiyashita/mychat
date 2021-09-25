import Dependencies._

ThisBuild / scalaVersion := "3.0.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalacOptions ++= "-deprecation" :: "-feature" :: Nil,
  scalafmtOnCompile := true
)

lazy val testSettings = Seq(libraryDependencies += scalaTest)

lazy val root = (project in file("."))
  .settings(
    name := "mychat",
    testSettings,
    libraryDependencies ++= Seq(
      circe,
      akka
    ).flatten.map(_.cross(CrossVersion.for3Use2_13))
  )
