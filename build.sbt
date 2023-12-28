ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "occ-crawler-scala"
  )

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.17.1",
  "org.apache.commons" % "commons-csv" % "1.10.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
   "ch.qos.logback" % "logback-classic" % "1.4.14"

)