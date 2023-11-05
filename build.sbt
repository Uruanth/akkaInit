ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val akkaVersion = "2.8.3"

lazy val root = (project in file("."))
  .settings(
    name := "akkaEsse1",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
//      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
//      "org.scalatest" %% "scalatest" % "3.1.0" % Test
      "org.scalatest" %% "scalatest" % "3.1.0"
    )
  )
