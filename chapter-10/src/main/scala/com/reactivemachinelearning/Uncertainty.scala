package com.reactivemachinelearning

object Uncertainty {

  sealed trait UncertainSentiment {
    def confidence: Double
  }

  case object STRONG_LIKE extends UncertainSentiment {
    val confidence = 0.90
  }

  case object INDIFFERENT extends UncertainSentiment {
    val confidence = 0.50
  }

  case object DISLIKE extends UncertainSentiment {
    val confidence = 0.30
  }

}
