package com.reactivemachinelearning

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.view.ViewQuery
import com.couchbase.spark._
import com.reactivemachinelearning.FeatureGeneration.{IntFeature, BooleanFeature, Feature}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object DatabaseInteraction extends App {

  // Configure Spark
  val conf = new SparkConf()
    .setAppName("couchbaseQuickstart")
    .setMaster("local[*]")
    .set("com.couchbase.bucket.default", "")

  // Generate The Context
  val sc = new SparkContext(conf)

  val rawSquawks: RDD[JsonDocument] = sc.couchbaseView(
    ViewQuery.from("squawks", "by_squawk_id"))
    .map(_.id)
    .couchbaseGet[JsonDocument]()

  rawSquawks.foreach(println)


  def extract(rawSquawks: RDD[JsonDocument]): RDD[IntFeature] = {
    ???
  }

  def transform(inputFeatures: RDD[IntFeature]): RDD[BooleanFeature] = {
    ???
  }

  val trainableFeatures = transform(extract(rawSquawks))
}
