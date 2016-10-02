package com.reactivemachinelearning

import org.http4s._
import org.http4s.dsl._

import scala.util.Random

object Models {

  val modelA = HttpService {
    case GET -> Root / "a" / inputData =>
      val response = true
      Ok(s"Model A predicted $response.")
  }

  val modelB = HttpService {
    case GET -> Root / "b" / inputData =>
      val response = false
      Ok(s"Model B predicted $response.")
  }

  val modelC = HttpService {
    case GET -> Root / "c" / inputData => {

      val workingOk = Random.nextBoolean()

      val response = true

      if (workingOk) {
        Ok(s"Model C predicted $response.")
      } else {
        BadRequest("Model C failed to predict.")
      }
    }
  }

}


