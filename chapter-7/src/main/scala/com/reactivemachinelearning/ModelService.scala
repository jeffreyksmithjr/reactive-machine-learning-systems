package com.reactivemachinelearning

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
//import spray.json._
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}

case class Prediction(id: Long, timestamp: Long, value: Double)

trait Protocols extends DefaultJsonProtocol {
  implicit val ipInfoFormat = jsonFormat3(Prediction.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val logger: LoggingAdapter

//  private def parseFeatures(features: String): Map[Long, Double] = {
//    features.parseJson.convertTo[Map[Long, Double]]
//  }

  def predict(features: String): Future[Prediction] = {
    Future(Prediction(123, 456, 0.5))
  }

  val routes = {
    logRequestResult("predictive-service") {
      pathPrefix("ip") {
        (get & path(Segment)) { features =>
          complete {
            predict(features).map[ToResponseMarshallable] {
//              case prediction: Prediction => prediction
              case _ => BadRequest
            }
          }
        }
      }
    }
  }
}

object PredictiveService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "0.0.0.0", 9000)
}