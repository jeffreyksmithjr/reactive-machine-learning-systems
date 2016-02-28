package com.reactivemachinelearning.stubs

// stub implementations
trait Instance
trait Feature
trait Label

class Model(features: List[Instance]) {

  def predict(features: Set[Feature]): Label = ???

}
