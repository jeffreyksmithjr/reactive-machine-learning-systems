package com.reactivemachinelearning

import scala.collection.mutable

class LearningAgent {

  import com.reactivemachinelearning.LearningAgent._

  val knowledge = new mutable.HashMap[String, Sentiment]()

  def observe(thing: String, sentiment: Sentiment): Unit = {
    knowledge.put(thing, sentiment)
  }

  def doYouLike(thing: String): Boolean = {
    knowledge.getOrElse(thing, DISLIKE).asBoolean
  }

  val learnedDislikes = new mutable.HashSet[Char]()

  def learn() = {

    val Vowels = Set[Char]('a', 'e', 'i', 'o', 'u', 'y')

    knowledge.foreach({
      case (thing: String, sentiment: Sentiment) => {
        val thingVowels = thing.toSet.filter(Vowels.contains)
        if (sentiment == DISLIKE) {
          thingVowels.foreach(learnedDislikes.add)
        }
      }
    }
    )
  }

  def doYouReallyLike(thing: String): Boolean = {
    thing.toSet.forall(!learnedDislikes.contains(_))
  }

}

object LearningAgent {

  sealed trait Sentiment {
    def asBoolean: Boolean
  }

  case object LIKE extends Sentiment {
    val asBoolean = true
  }

  case object DISLIKE extends Sentiment {
    val asBoolean = false
  }

}




