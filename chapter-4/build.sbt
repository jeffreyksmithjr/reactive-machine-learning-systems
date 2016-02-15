name := "chapter-4"

version := "1.0"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.6.0" ,
  "org.apache.spark" %% "spark-mllib" % "1.6.0" ,
  "com.github.nscala-time" %% "nscala-time" % "2.8.0",
  "com.couchbase.client" %% "spark-connector" % "1.0.1")
    