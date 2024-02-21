organization := "com.phishscan"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "PhishScan"
  )

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
libraryDependencies += "ch.qos.logback"              % "logback-classic" % "1.4.12"
