package com.reactivemachinelearning

class IntelligentAgent(likes: Set[String]) {

  def doYouLike(thing: String): Boolean = {
    likes.contains(thing)
  }

}
