name := "chapter-4"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "2.2.0" ,
  "org.apache.spark" %% "spark-mllib" % "2.2.0" ,
  "com.github.nscala-time" %% "nscala-time" % "2.8.0",
  "com.couchbase.client" %% "spark-connector" % "2.2.0")
    