import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9"

  val circeVersion = "0.12.3"
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  val AkkaVersion     = "2.6.8"
  val AkkaHttpVersion = "10.2.0"
  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-actor-typed"  % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream"       % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http"         % AkkaHttpVersion
  )

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.0"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.6"

}
