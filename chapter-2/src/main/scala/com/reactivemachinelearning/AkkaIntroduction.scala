package com.reactivemachinelearning

import akka.actor.SupervisorStrategy.Restart
import akka.actor._

import scala.collection.mutable
import scala.util.Random

object AkkaIntroduction extends App {

  val system = ActorSystem("voting")

  val connection = new DatabaseConnection("http://remotedatabase")
  val writerProps = Props(new VoteWriter(connection))
  val writerSuperProps = Props(new WriterSupervisor(writerProps))

  val votingSystem = system.actorOf(writerSuperProps)

  votingSystem ! Vote(1, 5, "nom nom")
  votingSystem ! Vote(2, 7, "Mikey")
  votingSystem ! Vote(3, 9, "nom nom")

  println(connection.votes)

}

case class Vote(timestamp: Long, voterId: Long, howler: String)

class VoteWriter(connection: DatabaseConnection) extends Actor {
  def receive = {
    case Vote(timestamp, voterId, howler) =>
      connection.insert(Map("timestamp" -> timestamp,
        "voterId" -> voterId,
        "howler" -> howler))
  }
}

class WriterSupervisor(writerProps: Props) extends Actor {
  override def supervisorStrategy = OneForOneStrategy() {
    case exception: Exception => Restart
  }

  val writer = context.actorOf(writerProps)

  def receive = {
    case message => writer forward message
  }
}

class DatabaseConnection(url: String) {
  var votes = new mutable.HashMap[String, Any]()

  def insert(updateMap: Map[String, Any]) = {
    if (Random.nextBoolean()) throw new Exception

    updateMap.foreach {
      case (key, value) => votes.update(key, value)
    }
  }
}