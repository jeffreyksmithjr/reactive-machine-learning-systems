package com.reactivemachinelearning

import org.http4s.Response
import org.http4s.Uri
import org.http4s.client.blaze.PooledHttp1Client

import scala.concurrent.duration._
import scalaz.concurrent.Task

object Client {

  val client = PooledHttp1Client()

  private def call(model: String, input: String): Task[Response] = {
    val target = Uri.fromString(s"http://localhost:8080/models/$model/$input").toOption.get
    //    client.expect[String](target)
    client(target).retry(Seq(1 second), { _ => true })
  }

  def callA(input: String) = call("a", input)

  def callB(input: String) = call("b", input)

  def callC(input: String) = call("c", input)

}
