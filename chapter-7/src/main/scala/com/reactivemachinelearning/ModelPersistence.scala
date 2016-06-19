package com.reactivemachinelearning

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

object ModelPersistence extends App {

  val conf = new SparkConf().setAppName("ModelPersistence").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  import sqlContext.implicits._

  val tokenizer = new Tokenizer()
    .setInputCol("text")
    .setOutputCol("words")
  val hashingTF = new HashingTF()
    .setInputCol(tokenizer.getOutputCol)
    .setOutputCol("features")
  val lr = new LogisticRegression()
    .setMaxIter(3)
  val pipeline = new Pipeline()
    .setStages(Array(tokenizer, hashingTF, lr))

  val paramMaps = new ParamGridBuilder()
    .addGrid(hashingTF.numFeatures, Array(10, 20))
    .addGrid(lr.regParam, Array(0.0, 0.1))
    .build()
  // Note: In practice, you would likely want to use more features and more iterations for LogisticRegression.
  val evaluator = new BinaryClassificationEvaluator()

  val cv = new CrossValidator()
    .setEstimator(pipeline)
    .setEvaluator(evaluator)
    .setNumFolds(2)
    .setEstimatorParamMaps(paramMaps)

  val data = Seq(
    ("Hello hello world", 0.0),
    ("Hello how are you world", 0.0),
    ("What is the world", 1.0),
    ("There was a plant in the water", 0.0),
    ("Water was around the world", 1.0),
    ("And then hello again", 0.0))
  val df = sqlContext.createDataFrame(data).toDF("text", "label")

  val cvModel = cv.fit(df)

  cvModel.save("my-model")


}
