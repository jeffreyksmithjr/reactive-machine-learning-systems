package com.reactivemachinelearning

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

import com.github.nscala_time.time.Imports._


object FeatureCollections extends App {


  // setup
  val conf = new SparkConf().setAppName("Feature Generation").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  import sqlContext.implicits._


  trait FeatureType[+V] extends Serializable {
    val name = this.getClass.getSimpleName
  }

  trait LabelType[+V] extends FeatureType[V]

  object SquawkLength extends FeatureType[Int]

  object Super extends LabelType[Boolean]

  val originalFeatures = Set(SquawkLength)
  val label = Super

  object PastSquawks extends FeatureType[Int]

  val basicFeatures = Set(SquawkLength, PastSquawks)

  object MobileSquawker extends FeatureType[Boolean]

  val moreFeatures = Set(SquawkLength, PastSquawks, MobileSquawker)

  case class FeatureCollection(id: Int,
                               createdAt: DateTime,
                               features: Set[_ <: FeatureType[Any]],
                               label: LabelType[Any])


  val earlier = (DateTime.now - 1 month).getDateTime
  val now = DateTime.now()

  val earlierCollection = FeatureCollection(101,
    earlier,
    basicFeatures,
    label)

  val latestCollection = FeatureCollection(202,
    now,
    moreFeatures,
    label)

  val featureCollections = sc.parallelize(
    Seq(earlierCollection,
      latestCollection))

  val beginningOfTime = (DateTime.now() - 1 year).getDateTime

  val FallbackCollection = FeatureCollection(404,
    beginningOfTime,
    originalFeatures,
    label)

  def validCollection(collections: RDD[FeatureCollection],
                      invalidFeatures: Set[FeatureType[Any]]) = {
    val validCollections = collections.filter(
      fc => !fc.features.exists(invalidFeatures.contains))
      .sortBy(collection => collection.id)
    if (validCollections.count() > 0) {
      validCollections.first()
    } else
      FallbackCollection
  }

  val usableCollection = validCollection(featureCollections, Set(MobileSquawker))

  case class ValidatedFeatureCollection(id: Int,
                                        createdAt: DateTime,
                                        features: Set[_ <: FeatureType[Any]],
                                        label: LabelType[Any],
                                        passedValidation: Boolean)

}
