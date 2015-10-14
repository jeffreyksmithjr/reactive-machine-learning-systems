name := "chapter-3"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++=  Seq("ReactiveCouchbase Snapshots" at "https://raw.github.com/ReactiveCouchbase/repository/master/snapshots/",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq("org.reactivecouchbase" %% "reactivecouchbase-core" % "0.4-SNAPSHOT",
  "com.typesafe.play" %% "play-json" % "2.3.0")
