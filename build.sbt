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

libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.10.0"

