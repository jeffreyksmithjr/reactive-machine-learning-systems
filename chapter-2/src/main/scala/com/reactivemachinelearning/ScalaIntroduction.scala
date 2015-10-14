package com.reactivemachinelearning

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object ScalaIntroduction extends App {

  val totalVotes = Map("Mikey" -> 52, "nom nom" -> 105)

  val naiveNomNomVotes: Option[Int] = totalVotes.get("nom nom")

  def getVotes(howler: String) = {
    totalVotes.get(howler) match {
      case Some(votes) => votes
      case None => 0
    }
  }

  val nomNomVotes: Int = getVotes("nom nom")
  val indianaVotes: Int = getVotes("Indiana")

  val totalVotesWithDefault = Map("Mikey" -> 52, "nom nom" -> 105).withDefaultValue(0)

  def getRemoteVotes(howler: String): Int = {
    Thread.sleep(Random.nextInt(1000))
    totalVotesWithDefault(howler)
  }

  val mikeyVotes = getRemoteVotes("Mikey")

  implicit val ec = ExecutionContext.Implicits.global

  def futureRemoteVotes(howler: String) = Future {
    getRemoteVotes(howler)
  }

  val nomNomFutureVotes = futureRemoteVotes("nom nom")
  val mikeyFutureVotes = futureRemoteVotes("Mikey")
  val indianaFutureVotes = futureRemoteVotes("Indiana")

  val topDogVotes: Future[Int] = for {
    nomNom <- nomNomFutureVotes
    mikey <- mikeyFutureVotes
    indiana <- indianaFutureVotes
  } yield List(nomNom, mikey, indiana).max


  topDogVotes onSuccess {
    case _ => println("The top dog currently has" + topDogVotes + "votes.")
  }

  val timeoutDuration = 500
  val AverageVotes = 42

  val defaultVotes = Future {
    Thread.sleep(timeoutDuration)
    AverageVotes
  }

  def timeOutVotes(howler: String) = Future.firstCompletedOf(
    List(futureRemoteVotes(howler), defaultVotes))
}
