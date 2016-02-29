package com.reactivemachinelearning

trait FeatureType[V] {
  val name: String
}

trait Feature[V] extends FeatureType[V] {
  val value: V
}

trait Label[V] extends Feature[V]

case class BooleanFeature(name: String, value: Boolean) extends Feature[Boolean]

case class BooleanLabel(name: String, value: Boolean) extends Label[Boolean]

case class BooleanInstance(features: Set[BooleanFeature], label: BooleanLabel)

class NaiveBayesModel(instances: List[BooleanInstance]) {

  val trueInstances = instances.filter(i => i.label.value)
  val probabilityTrue = trueInstances.size.toDouble / instances.size

  val featureTypes = instances.flatMap(i => i.features.map(f => f.name)).toSet

  val featureProbabilities = featureTypes.toList.map {
    featureType =>
      trueInstances.map { i =>
        i.features.filter { f =>
          f.name equals featureType
        }.count {
          f => f.value
        }
      }.sum.toDouble / trueInstances.size
  }

  val numerator = featureProbabilities.reduceLeft(_ * _) * probabilityTrue

  def probabilityFeatureVector(features: Set[BooleanFeature]) = {
    val matchingInstances = instances.count(i => i.features == features)
    matchingInstances.toDouble / instances.size
  }

  def predict(features: Set[BooleanFeature]) = {
    numerator / probabilityFeatureVector(features)
  }

}
