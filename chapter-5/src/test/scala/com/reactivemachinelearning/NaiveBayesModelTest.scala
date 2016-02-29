package com.reactivemachinelearning

import org.scalatest.FunSuite

class NaiveBayesModelTest extends FunSuite {


  test("It can learn a model and predict") {
    val trainingInstances = List(
      BooleanInstance(
        Set(BooleanFeature("food", true),
          BooleanFeature("goOut", true),
          BooleanFeature("cubs", true)),
        BooleanLabel("match", true)),
      BooleanInstance(
        Set(BooleanFeature("food", true),
          BooleanFeature("goOut", true),
          BooleanFeature("cubs", false)),
        BooleanLabel("match", false)),
      BooleanInstance(
        Set(BooleanFeature("food", true),
          BooleanFeature("goOut", true),
          BooleanFeature("cubs", false)),
        BooleanLabel("match", false)))

    val testFeatureVector = Set(BooleanFeature("food", true),
      BooleanFeature("goOut", true),
      BooleanFeature("cubs", false))

    val model = new NaiveBayesModel(trainingInstances)

    val prediction = model.predict(testFeatureVector)

    assert(prediction == 0.5)
  }

}
