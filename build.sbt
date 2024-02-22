import scala.collection.immutable.Seq

organization := "com.phishscan"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "PhishScan"
  )

resolvers += "Google's Maven Public Repository" at "https://maven.google.com/"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
libraryDependencies += "ch.qos.logback"              % "logback-classic" % "1.4.12"

libraryDependencies += "com.squareup.okhttp3" % "okhttp"  % "4.10.0"
libraryDependencies += "info.picocli"         % "picocli" % "4.7.3"

libraryDependencies += "io.monix"                %% "monix-kafka-1x"          % "1.0.0-RC7" exclude ("org.slf4j", "log4j-over-slf4j")
libraryDependencies += "info.picocli"             % "picocli"                 % "4.7.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.3")

dependencyOverrides += "org.apache.kafka" % "kafka-clients" % "3.3.1"
