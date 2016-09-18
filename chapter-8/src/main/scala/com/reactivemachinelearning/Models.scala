package com.reactivemachinelearning

import org.http4s._
import org.http4s.dsl._

import scala.util.Random

object Models {

  val modelA = HttpService {
    case GET -> Root / "a" / inputData =>
      val response = Random.nextBoolean()
      Ok(s"Model A predicted $response.")
  }

  val modelB = HttpService {
    case GET -> Root / "b" / inputData =>
      val response = Random.nextBoolean()
      Ok(s"Model B predicted $response.")
  }

}


