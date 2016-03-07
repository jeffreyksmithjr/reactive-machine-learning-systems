package com.reactivemachinelearning

import com.reactivemachinelearning.ModelType.ModelType
import net.razorvine.pyro.{NameServerProxy, PyroProxy}

import scala.concurrent.{ExecutionContext, Future}

object ModelType extends Enumeration {
  type ModelType = Value
  val VGG = Value("VGG")
  val I2V = Value("I2V")
}

case class JobConfiguration(contentPath: String,
                            stylePath: String,
                            modelPath: String,
                            modelType: ModelType,
                            width: Integer = 800,
                            alpha: java.lang.Double = 1.0,
                            beta: java.lang.Double = 200.0,
                            iterations: Integer = 5000)

object NeuralFacade extends App {
  implicit val ec = ExecutionContext.Implicits.global

  val jobConfiguration = JobConfiguration("/Users/jeff/Downloads/sloth_bear.png",
    "/Users/jeff/Documents/Projects/art-style/neural-style/startup-style/styles/3-thu-senecio-1922-paul-klee.jpg",
    "/Users/jeff/Documents/Projects/neural-art-tf/dekaffed",
    ModelType.VGG,
    iterations = 1000)

  val ns = NameServerProxy.locateNS(null)
  val remoteServer = new PyroProxy(ns.lookup("neuralserver"))

  val result = callServer(remoteServer, jobConfiguration)

  result.onComplete(resultValue => assert(resultValue.get, "Learning timed out."))

  println("Shutting down.")
  remoteServer.close()
  ns.close()


  val timeoutDuration = 60 * 60 * 1000 // 1 hour

  def timedOut = Future {
    Thread.sleep(timeoutDuration)
    false
  }

  def callServer(remoteServer: PyroProxy, jobConfiguration: JobConfiguration) = {
    Future.firstCompletedOf(
      List(
        timedOut,
        Future {
          remoteServer.call("generate",
            jobConfiguration.contentPath,
            jobConfiguration.stylePath,
            jobConfiguration.modelPath,
            jobConfiguration.modelType.toString,
            jobConfiguration.width,
            jobConfiguration.alpha,
            jobConfiguration.beta,
            jobConfiguration.iterations).asInstanceOf[Boolean]
        }))
  }
}
