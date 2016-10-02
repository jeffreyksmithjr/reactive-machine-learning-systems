package com.reactivemachinelearning

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.dsl._


import scalaz.concurrent.Task

object ModelSupervisor extends ServerApp {

  def splitTraffic(data: String) = {
    data.hashCode % 10 match {
      case x if x < 4 => Client.callA(data)
      case x if x < 6 => Client.callB(data)
      case _ => Client.callC(data)
    }
  }

  val apiService = HttpService {
    case GET -> Root / "predict" / inputData =>
      val response = splitTraffic(inputData).run

      response match {
        case r: Response if r.status == Ok => Response(Ok).withBody(r.bodyAsText)
        case r => Response(BadRequest).withBody(r.bodyAsText)
      }
  }

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindLocal(8080)
      .mountService(apiService, "/api")
      .mountService(Models.modelA, "/models")
      .mountService(Models.modelB, "/models")
      .mountService(Models.modelC, "/models")
      .start
  }

}
