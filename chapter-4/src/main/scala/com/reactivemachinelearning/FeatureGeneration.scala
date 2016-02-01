package com.reactivemachinelearning

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{ChiSqSelector, HashingTF, Tokenizer}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SQLContext

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

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


  trait FeatureType {
    val name: String
    type V
  }

  trait Feature extends FeatureType {
    val value: V
  }

  case class WordSequenceFeature(name: String, value: Seq[String]) extends Feature {
    type V = Seq[String]
  }

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


  case class IntFeature(name: String, value: Int) extends Feature {
    type V = Int
  }

  case class BooleanFeature(name: String, value: Boolean) extends Feature {
    type V = Boolean
  }

  def binarize(feature: IntFeature, threshold: Double): BooleanFeature = {
    BooleanFeature("binarized-" + feature.name, feature.value > threshold)
  }

  val SUPER_THRESHOLD = 1000000

  val squirrelFollowers = 12
  val slothFollowers = 23584166

  val squirrelFollowersFeature = IntFeature("followers", squirrelFollowers)
  val slothFollowersFeature = IntFeature("followers", slothFollowers)

  val squirrelIsSuper = binarize(squirrelFollowersFeature, SUPER_THRESHOLD)
  val slothIsSuper = binarize(slothFollowersFeature, SUPER_THRESHOLD)


  trait Label extends Feature

  case class BooleanLabel(name: String, value: Boolean) extends Label {
    type V = Boolean
  }

  def toBooleanLabel(feature: BooleanFeature) = {
    BooleanLabel(feature.name, feature.value)
  }

  val squirrelLabel = toBooleanLabel(squirrelIsSuper)
  val slothLabel = toBooleanLabel(slothIsSuper)

  Seq(squirrelLabel, slothLabel).foreach(println)

  val instances = Seq(
    (123, Vectors.dense(0.2, 0.3, 16.2, 1.1), 0.0),
    (456, Vectors.dense(0.1, 1.3, 11.3, 1.2), 1.0),
    (789, Vectors.dense(1.2, 0.8, 14.5, 0.5), 0.0)
  )

  val featuresName = "features"
  val labelName = "isSuper"

  val instancesDF = sqlContext.createDataFrame(instances)
    .toDF("id", featuresName, labelName)

  val selector = new ChiSqSelector()
    .setNumTopFeatures(2)
    .setFeaturesCol(featuresName)
    .setLabelCol(labelName)
    .setOutputCol("selectedFeatures")

  val selectedFeatures = selector.fit(instancesDF)
    .transform(instancesDF)

  selectedFeatures.show()

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

    import com.reactivemachinelearning.FeatureGeneration.CategoricalTransforms.categorize

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

  trait StubGenerator extends Generator {
    def generate(squawk: Squawk) = {
      IntFeature("dummyFeature", Random.nextInt())
    }
  }

  object SquawkLanguage extends StubGenerator {}

  object HasImage extends StubGenerator {}

  object UserData extends StubGenerator {}

  val featureGenerators = Set(SquawkLanguage, HasImage, UserData)


  object GlobalUserData extends StubGenerator {}

  object RainforestUserData extends StubGenerator {}

  val globalFeatureGenerators = Set(SquawkLanguage, HasImage, GlobalUserData)

  val rainforestFeatureGenerators = Set(SquawkLanguage, HasImage, RainforestUserData)


  trait RainforestData {
    self =>
    require(rainforestContext(),
      s"${self.getClass} uses rainforest data outside of a rainforest context.")

    private def rainforestContext() = {
      val environment = Option(System.getenv("RAINFOREST"))
      environment.isDefined && environment.get.toBoolean
    }
  }

  object SafeRainforestUserData extends StubGenerator with RainforestData {}

  val safeRainforestFeatureGenerators = Set(SquawkLanguage, HasImage, SafeRainforestUserData)

}

