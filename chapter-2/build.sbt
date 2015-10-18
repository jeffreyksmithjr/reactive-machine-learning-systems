name := "chapter-2"

version := "1.0"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.5.1" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.5.1" % "provided",
  "com.typesafe.akka" %% "akka-actor" % "2.3.10")