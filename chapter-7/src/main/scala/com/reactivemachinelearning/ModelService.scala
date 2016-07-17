package com.reactivemachinelearning

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.util.Random

case class Prediction(id: Long, timestamp: Long, value: Double)


trait Protocols extends DefaultJsonProtocol {
  implicit val predictionFormat = jsonFormat3(Prediction.apply)
}

trait Service extends Protocols {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val logger: LoggingAdapter

  private def parseFeatures(features: String): Map[String, Double] = {
    features.parseJson.convertTo[Map[Char, Double]]
  }

  private def model(features: Map[Char, Double]) = {
    val coefficients = ('a' to 'z').zip(1 to 26).toMap
    val predictionValue = features.map {
      case (identifier, value) =>
        coefficients.getOrElse(identifier, 0) * value
    }.sum / features.size
    Prediction(Random.nextLong(), System.currentTimeMillis(), predictionValue)
  }

  private def predict(features: String): Prediction = {
    model(parseFeatures(features))
  }

  val routes = {
    logRequestResult("model-service") {
      pathPrefix("predict") {
        (get & path(Segment)) {
          features: String =>
            complete {
              ToResponseMarshallable(predict(features))
            }
        }
      }
    }
  }
}

object ModelService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "0.0.0.0", 9000)
}
