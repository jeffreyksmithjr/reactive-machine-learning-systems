package com.reactivemachinelearning

import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object FeatureGeneration extends App {

  // setup
  val conf = new SparkConf().setAppName("Feature Generation").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  case class Squawk(id: Int, text: String)

  // Input data: Each row is a 140 character or less squawk
  val squawks = sqlContext.createDataFrame(Seq(
    Squawk(123, "Clouds sure make it hard to look on the bright side of things."),
    Squawk(124, "Who really cares who gets the worm?  I'm fine with sleeping in."),
    Squawk(125, "Why don't french fries grow on trees?"))).toDF("label", "squawk")

  val tokenizer = new Tokenizer().setInputCol("squawk").setOutputCol("words")

  val tokenized = tokenizer.transform(squawks)
  tokenized.select("words", "label").take(3).foreach(println)

}
