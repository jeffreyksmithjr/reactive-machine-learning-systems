package com.reactivemachinelearning

import com.reactivemachinelearning.ModelType.ModelType
import net.razorvine.pyro.{PyroProxy, NameServerProxy}

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

  val jobConfiguration = JobConfiguration("/Users/jeff/Downloads/sloth_bear.png",
    "/Users/jeff/Documents/Projects/art-style/neural-style/startup-style/styles/4-fri-Vassily_Kandinsky,_1913_-_Composition_7.jpg",
    "/Users/jeff/Documents/Projects/neural-art-tf/dekaffed-iv",
    ModelType.I2V,
    iterations = 100)

  val ns = NameServerProxy.locateNS(null)
  val remoteServer = new PyroProxy(ns.lookup("neuralserver"))
  val result = callServer(remoteServer, jobConfiguration)
  val message = result.toString
  System.out.println("result message=" + message)
  remoteServer.close()
  ns.close()


  def callServer(remoteServer: PyroProxy, jobConfiguration: JobConfiguration) = {
    remoteServer.call("generate",
      jobConfiguration.contentPath,
      jobConfiguration.stylePath,
      jobConfiguration.modelPath,
      jobConfiguration.modelType.toString,
      jobConfiguration.width,
      jobConfiguration.alpha,
      jobConfiguration.beta,
      jobConfiguration.iterations)

  }
}
