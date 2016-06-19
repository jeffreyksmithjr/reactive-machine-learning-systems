name := "chapter-7"

version := "1.0"

scalaVersion := "2.11.4"

val akkaV = "2.4.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.0.0-preview",
  "org.apache.spark" %% "spark-mllib" % "2.0.0-preview",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.github.nscala-time" %% "nscala-time" % "2.8.0",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV)
