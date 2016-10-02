package com.reactivemachinelearning

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz.concurrent.Task

object Tasking extends App {

  def doStuff(source: String) = println(s"Doing $source stuff")

  val futureVersion = Future(doStuff("Future"))

  Thread.sleep(1000)

  println("After Future instantiation")

  val taskVersion = Task(doStuff("Task"))

  Thread.sleep(1000)

  println("After Task instantiation")

  taskVersion.run

}
