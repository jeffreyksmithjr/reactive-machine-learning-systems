package com.reactivemachinelearning

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object FeatureGeneration extends App {

  // setup
  val conf = new SparkConf().setAppName("Feature Generation").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)


  case class Squawk(id: Int, text: String)

  // Input data: Each row is a 140 character or less squawk
  val squawks = Seq(Squawk(123, "Clouds sure make it hard to look on the bright side of things."),
    Squawk(124, "Who really cares who gets the worm?  I'm fine with sleeping in."),
    Squawk(125, "Why don't french fries grow on trees?"))

  val squawksDF = sqlContext.createDataFrame(squawks).toDF("squawkId", "squawk")

  val tokenizer = new Tokenizer().setInputCol("squawk").setOutputCol("words")

  val tokenized = tokenizer.transform(squawksDF)
  tokenized.select("words", "squawkId").foreach(println)


  trait Feature {
    val name: String
    val value: Any
  }

  case class WordSequenceFeature(name: String, value: Seq[String]) extends Feature

  val wordsFeatures = tokenized.select("words")
    .map[WordSequenceFeature](row =>
    WordSequenceFeature("words",
      row.getSeq[String](0)))

  wordsFeatures.foreach(println)

  val hashingTF = new HashingTF()
    .setInputCol("words")
    .setOutputCol("termFrequencies")

  val tfs = hashingTF.transform(tokenized)

  tfs.select("termFrequencies").foreach(println)


  val pipeline = new Pipeline()
    .setStages(Array(tokenizer, hashingTF))

  val pipelineHashed = pipeline.fit(squawksDF)

  println(pipelineHashed.getClass)


  case class IntFeature(name: String, value: Int) extends Feature

  case class BooleanFeature(name: String, value: Boolean) extends Feature

  def binarize(feature: IntFeature, threshold: Double): BooleanFeature = {
    BooleanFeature(feature.name, feature.value > threshold)
  }

  val SUPER_THRESHOLD = 1000000

  val squirrelFollowers = IntFeature("followers", 12)
  val slothFollowers = IntFeature("followers", 23584166)

  val squirrelIsSuper = binarize(squirrelFollowers, SUPER_THRESHOLD)
  val slothIsSuper = binarize(slothFollowers, SUPER_THRESHOLD)


  trait Label extends Feature

  case class BooleanLabel(name: String, value: Boolean) extends Label

  def toBooleanLabel(feature: BooleanFeature) = {
    BooleanLabel(feature.name, feature.value)
  }

  val squirrelLabel = toBooleanLabel(squirrelIsSuper)
  val slothLabel = toBooleanLabel(slothIsSuper)

  Seq(squirrelLabel, slothLabel).foreach(println)


  trait Generator {

    def generate(squawk: Squawk): Feature

  }

  object SquawkLengthCategory extends Generator {

    val ModerateSquawkThreshold = 47
    val LongSquawkThreshold = 94

    private def extract(squawk: Squawk): IntFeature = {
      IntFeature("squawkLength", squawk.text.length)
    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val squawkLengthCategory = lengthFeature match {
        case IntFeature(_, length) if length < ModerateSquawkThreshold => 1
        case IntFeature(_, length) if length < LongSquawkThreshold => 2
        case _ => 3
      }

      IntFeature("squawkLengthCategory", squawkLengthCategory)
    }

    def generate(squawk: Squawk): IntFeature = {
      transform(extract(squawk))
    }
  }


  object CategoricalTransforms {

    def categorize(thresholds: List[Int]): (Int) => Int = {
      (dataPoint: Int) => {
        thresholds.sorted
          .zipWithIndex
          .find {
          case (threshold, i) => dataPoint < threshold
        }.getOrElse((None, -1))
          ._2
      }
    }
  }

  object SquawkLengthCategoryRefactored extends Generator {

    import CategoricalTransforms.categorize

    val Thresholds = List(47, 94, 141)

    private def extract(squawk: Squawk): IntFeature = {
      IntFeature("squawkLength", squawk.text.length)
    }

    private def transform(lengthFeature: IntFeature): IntFeature = {
      val squawkLengthCategory = categorize(Thresholds)(lengthFeature.value)
      IntFeature("squawkLengthCategory", squawkLengthCategory)
    }

    def generate(squawk: Squawk): IntFeature = {
      transform(extract(squawk))
    }
  }

}

