name := "chapter-5"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies := List("org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.apache.spark" %% "spark-core" % "2.2.0",
  "org.apache.spark" %% "spark-mllib" % "2.2.0",
  "net.razorvine" % "pyrolite" % "4.10")