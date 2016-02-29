package com.reactivemachinelearning

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object SparkPipeline extends App {

  // setup
  val conf = new SparkConf().setAppName("TimberPipeline").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  val data = sqlContext.read.format("libsvm").load("/Users/jeff/Documents/Projects/reactive-machine-learning-systems/chapter-5/src/main/resources/match_data.libsvm")

  // Index labels, adding metadata to the label column.
  // Fit on whole dataset to include all labels in index.
  val labelIndexer = new StringIndexer()
    .setInputCol("label")
    .setOutputCol("indexedLabel")
    .fit(data)
  // Automatically identify categorical features, and index them.
  val featureIndexer = new VectorIndexer()
    .setInputCol("features")
    .setOutputCol("indexedFeatures")
    .setMaxCategories(4) // features with > 4 distinct values are treated as continuous
    .fit(data)

  // Split the data into training and test sets (30% held out for testing)
  val Array(trainingData, testData) = data.randomSplit(Array(0.8, 0.2))

  // Train a DecisionTree model.
  val decisionTree = new DecisionTreeClassifier()
    .setLabelCol("indexedLabel")
    .setFeaturesCol("indexedFeatures")

  // Convert indexed labels back to original labels.
  val labelConverter = new IndexToString()
    .setInputCol("prediction")
    .setOutputCol("predictedLabel")
    .setLabels(labelIndexer.labels)

  // Chain indexers and tree in a Pipeline
  val pipeline = new Pipeline()
    .setStages(Array(labelIndexer, featureIndexer, decisionTree, labelConverter))

  // Train model.  This also runs the indexers.
  val model = pipeline.fit(trainingData)

  // Make predictions.
  val predictions = model.transform(testData)

  // Select example rows to display.
  predictions.select("predictedLabel", "label", "features").show(5)

  val treeModel = model.stages(2).asInstanceOf[DecisionTreeClassificationModel]
  println("Learned classification tree model:\n" + treeModel.toDebugString)

}
