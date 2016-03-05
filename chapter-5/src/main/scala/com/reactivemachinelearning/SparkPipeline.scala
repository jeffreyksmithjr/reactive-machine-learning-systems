package com.reactivemachinelearning

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier, DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object SparkPipeline extends App {

  val conf = new SparkConf().setAppName("TimberPipeline").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  val instances = sqlContext.read.format("libsvm").load("/Users/jeff/Documents/Projects/reactive-machine-learning-systems/chapter-5/src/main/resources/match_data.libsvm")

  val labelIndexer = new StringIndexer()
    .setInputCol("label")
    .setOutputCol("indexedLabel")
    .fit(instances)

  val featureIndexer = new VectorIndexer()
    .setInputCol("features")
    .setOutputCol("indexedFeatures")
    .fit(instances)

  val Array(trainingData, testingData) = instances.randomSplit(Array(0.8, 0.2))

  val decisionTree = new DecisionTreeClassifier()
    .setLabelCol("indexedLabel")
    .setFeaturesCol("indexedFeatures")

  val labelConverter = new IndexToString()
    .setInputCol("prediction")
    .setOutputCol("predictedLabel")
    .setLabels(labelIndexer.labels)

  val pipeline = new Pipeline()
    .setStages(Array(labelIndexer, featureIndexer, decisionTree, labelConverter))

  val model = pipeline.fit(trainingData)

  val predictions = model.transform(testingData)

  predictions.select("predictedLabel", "label", "features").show(1)

  val decisionTreeModel = model.stages(2)
    .asInstanceOf[DecisionTreeClassificationModel]

  println(decisionTreeModel.toDebugString)

  val randomForest = new RandomForestClassifier()
    .setLabelCol("indexedLabel")
    .setFeaturesCol("indexedFeatures")

  val revisedPipeline = new Pipeline()
    .setStages(Array(labelIndexer, featureIndexer, randomForest, labelConverter))

  val revisedModel = revisedPipeline.fit(trainingData)

  val randomForestModel = revisedModel.stages(2)
    .asInstanceOf[RandomForestClassificationModel]

  println(randomForestModel.toDebugString)

}
