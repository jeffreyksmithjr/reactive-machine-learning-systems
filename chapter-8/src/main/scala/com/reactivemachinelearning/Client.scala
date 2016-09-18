package com.reactivemachinelearning

import org.http4s.Uri
import org.http4s.client.blaze.PooledHttp1Client

object Client {

  val client = PooledHttp1Client()

  private def call(model: String, input: String) = {
    val target = Uri.fromString(s"http://localhost:8080/models/$model/$input").toOption.get
    client.expect[String](target)
  }

  def callA(input: String) = call("a", input)

  def callB(input: String) = call("b", input)

}
