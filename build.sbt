import Dependencies._

organization := "com.maciejmroz"
scalaVersion := "2.12.7"
version      := "0.1.0-SNAPSHOT"
name := "akka-money-transfer-service"

libraryDependencies ++= Seq(
  scalaTest % Test,
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "com.typesafe.akka" %% "akka-stream" % "2.5.17",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.17" % Test,
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "org.slf4j" % "slf4j-nop" % "1.7.25",
  "com.h2database" % "h2" % "1.4.197"
)

parallelExecution in Test := false
