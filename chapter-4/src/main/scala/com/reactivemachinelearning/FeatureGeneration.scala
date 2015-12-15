package com.reactivemachinelearning

import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object FeatureGeneration extends App {

  // setup
  val conf = new SparkConf().setAppName("Feature Generation").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  // Input data: Each row is a 140 character or less squawk
  val documentDF = sqlContext.createDataFrame(Seq(
    (0, "Clouds sure make it hard to look on the bright side of things."),
    (1, "Who really cares who gets the worm?  I'm fine with sleeping in."),
    (2, "Why don't french fries grow on trees?"))).toDF("label", "squawk")

  val tokenizer = new Tokenizer().setInputCol("squawk").setOutputCol("words")

  val tokenized = tokenizer.transform(documentDF)
  tokenized.select("words", "label").take(3).foreach(println)

}
