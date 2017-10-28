name := "chapter-2"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "2.2.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.2.0" % "provided",
  "com.typesafe.akka" %% "akka-actor" % "2.5.6")