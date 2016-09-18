import sbtdocker.DockerPlugin
import sbtdocker.immutable.Dockerfile

name := "chapter-8"

version := "1.0"

lazy val http4sVersion = "0.14.6"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)


mainClass in Compile := Some("com.reactivemachinelearning.Supervisor")

mainClass in assembly := Some("com.reactivemachinelearning.Supervisor")

enablePlugins(DockerPlugin)

dockerAutoPackageJavaApplication(exposedPorts = Seq(8080))