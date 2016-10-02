package com.reactivemachinelearning

import org.http4s.argonaut._
import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.dsl._

import scalaz.concurrent.Task

object ModelServer extends ServerApp {

//  implicit def responseEncoder: EntityEncoder[Response] = jsonEncoderOf[Response]

  def splitTraffic(data: String) = {
    data.hashCode % 10 match {
      case x if x < 4 => Client.callA(data)
      case _ => Client.callB(data)
    }
  }

  val apiService = HttpService {
    case GET -> Root / "predict" / inputData =>
      val response = splitTraffic(inputData).run
      Ok(response.toString())
  }

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindLocal(8080)
      .mountService(apiService, "/api")
      .mountService(Models.modelA, "/models")
      .mountService(Models.modelB, "/models")
      .start
  }

}