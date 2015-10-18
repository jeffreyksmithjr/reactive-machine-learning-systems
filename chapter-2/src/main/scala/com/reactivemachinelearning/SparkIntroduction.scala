package com.reactivemachinelearning

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.mllib.linalg.Vectors

object SparkIntroduction extends App {

  def main(args: Array[String]) {
    // handle args

    // setup
    val conf = new SparkConf().setAppName("ModelExample")
    val sc = new SparkContext(conf)

    // Load and parse the train and test data
    val inputBasePath = "example_data"
    val outputBasePath = "."
    val trainingDataPath = inputBasePath + "/training.txt"
    val testingDataPath = inputBasePath + "/testing.txt"
    val currentOutputPath = outputBasePath + System.currentTimeMillis()

    val trainingData = sc.textFile(trainingDataPath)
    val trainingParsed = trainingData.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }.cache()

    val testingData = sc.textFile(testingDataPath)
    val testingParsed = testingData.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }.cache()

    // Building the model
    val numIterations = 100
    val model = LinearRegressionWithSGD.train(trainingParsed, numIterations)

    // Evaluate model on testing examples
    val predictionsAndLabels = testingParsed.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Report performance statistics
    val metrics = new MulticlassMetrics(predictionsAndLabels)
    val precision = metrics.precision
    val recall = metrics.recall
    println(s"Precision: $precision Recall: $recall")

    // Save model
    model.save(sc, currentOutputPath)
  }
}

}
