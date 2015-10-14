package com.reactivemachinelearning

import java.util.concurrent.TimeUnit

import com.couchbase.client.protocol.views.Query
import org.reactivecouchbase.ReactiveCouchbaseDriver
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Success


object DataCollector extends App {

  implicit val ec = ExecutionContext.Implicits.global
  val timeout = Duration(10, TimeUnit.SECONDS)

  val driver = ReactiveCouchbaseDriver()
  val bucket = driver.bucket("default")


  case class PreyReading(sensorId: Int,
                         locationId: Int,
                         timestamp: Long,
                         animalsLowerBound: Double,
                         animalsUpperBound: Double,
                         percentZebras: Double)

  implicit val preyReadingFormatter = Json.format[PreyReading]

  def readingId(preyReading: PreyReading) = {
    List(preyReading.sensorId,
      preyReading.locationId,
      preyReading.timestamp).mkString("-")
  }

  val reading = PreyReading(36, 12, System.currentTimeMillis(), 12.0, 18.0, 0.60)

  println(readingId(reading))

  val setDoc = bucket.set[PreyReading](readingId(reading), reading)

  setDoc.onComplete {
    case Success(status) => println(s"Operation status: ${status.getMessage}")
    case _ => throw new Exception("Something went wrong")
  }


  Await.result(
    bucket.createDesignDoc("prey",
      """
        | {
        |     "views":{
        |        "by_sensor_id": {
        |            "map": "function (doc, meta) { emit(doc.sensorId, doc); } "
        |        }
        |     }
        | }
      """.stripMargin), timeout)


  Await.result(bucket.searchValues[PreyReading]("prey", "by_sensor_id")
    (new Query().setIncludeDocs(true))
    .enumerate.apply(Iteratee.foreach { doc =>
    println(s"Prey Reading: $doc")
  }), timeout)

  val retrievedReading = Await.result(
    bucket.searchValues[PreyReading]
      ("prey", "by_sensor_id")
      (new Query().setIncludeDocs(true)
        .setKey("36"))
      .toList,
    timeout)
    .head

  println(retrievedReading)


  import _root_.scala.util.Random

  val manyReadings = (1 to 100) map { index =>
    val reading = PreyReading(
      36,
      12,
      System.currentTimeMillis(),
      Random.nextInt(100).toDouble,
      Random.nextInt(100).toDouble,
      Random.nextFloat())
    (readingId(reading),
      reading)
  }

  val setManyReadings = bucket.setStream(Enumerator.enumerate(manyReadings))

  setManyReadings.map { results =>
    results.foreach(result => {
      if (result.isSuccess) println(s"Persisted: ${Json.prettyPrint(result.document.get)}")
      else println(s"Can't persist: $result")
    }
    )
  }


  Await.result(
    bucket.createDesignDoc("prey_36",
      """
        |{
        |    "views":
        |    {
        |        "by_timestamp":
        |        {
        |            "map":
        |            "function (doc, meta)  { if (doc.sensorId == 36) { emit(doc.timestamp, doc); } }"
        |        }
        |    }
        |}
      """.stripMargin), timeout)

  val lastTen = Await.result(
    bucket.searchValues[PreyReading]
      ("prey_36", "by_timestamp")
      (new Query().setIncludeDocs(true)
        .setDescending(true)
        .setLimit(10))
      .toList,
    timeout)

  val tenth = Await.result(
    bucket.searchValues[PreyReading]
      ("prey_36", "by_timestamp")
      (new Query().setIncludeDocs(true)
        .setDescending(true)
        .setSkip(9)
        .setLimit(1))
      .toList,
    timeout)

  Thread.sleep(1000)

  println(lastTen)
  println(tenth)


  val startOfDay = System.currentTimeMillis()
  val firstReading = PreyReading(36, 12, startOfDay, 86.0, 97.0, 0.90)
  val fourHoursLater = startOfDay + 4 * 60 * 60 * 1000
  val secondReading = PreyReading(36, 12, fourHoursLater, 0.0, 2.0, 1.00)

  val outOfOrderReadings = List(
    (readingId(secondReading), secondReading),
    (readingId(firstReading), firstReading))

  val setOutOfOrder = bucket.setStream(Enumerator.enumerate(outOfOrderReadings))

  setOutOfOrder.map { results =>
    results.foreach(result => {
      if (result.isSuccess) println(s"Persisted: ${Json.prettyPrint(result.document.get)}")
      else println(s"Can't persist: $result")
    }
    )
  }

  driver.shutdown()
}

