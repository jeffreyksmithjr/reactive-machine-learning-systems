package com.reactivemachinelearning

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.dsl._

import scalaz.concurrent.Task

object Supervisor extends ServerApp {

  def splitTraffic(data: String) = {
    data.hashCode % 10 match {
      case x if x < 4 => true
      case _ => false
    }
  }

  val service = HttpService {
    case GET -> Root / "predict" / inputData =>
      val response = splitTraffic(inputData)
      Ok(s"Predicted $response.")
  }

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(service, "/api")
      .start
  }
}