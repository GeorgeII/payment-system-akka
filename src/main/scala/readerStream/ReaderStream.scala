package readerStream

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.io.{Source => ScalaFileSource}

object ReaderStream {

  case class OnCompleteMessage(msg: String)

  private[readerStream] val config = ConfigFactory.load("application.conf")
  private[readerStream] val configField = "payments-folder"

  private[readerStream] def getAllFilesFromFolder: Vector[File] = {
    val filesFolder = new File(config.getString(configField))

    if (filesFolder.exists && filesFolder.isDirectory) {
      filesFolder.listFiles.filter(_.isFile).toVector
    }
    else {
      Vector[File]()
    }
  }

  private[readerStream] def readLinesInFile(file: File): Vector[String] = {
    val bufferedFileReader = ScalaFileSource.fromFile(file)
    val lines = bufferedFileReader.getLines.toVector
    bufferedFileReader.close()

    lines
  }

  /**
   * All files in the directory (assigned in config) -> all lines of all files ->
   * -> send every line to actor
   * @param sinkActor - an actor to which every line of all files are sent.
   * @return - RunnableGraph that can be run.
   */
  def buildReadingStream(sinkActor: ActorRef): RunnableGraph[NotUsed] = {
    import actors.PaymentChecker.CheckPayment

    val sourceFiles = Source(getAllFilesFromFolder)
    val linesFromFile = Flow[File].map(file => readLinesInFile(file))
    val flattening = Flow[Vector[String]].mapConcat(identity)
    val messageWrapping = Flow[String].map(payment => CheckPayment(payment))
    val sink = Sink.actorRef(
      sinkActor,
      OnCompleteMessage("The stream has completed successfully")
    )

    sourceFiles
      .via(linesFromFile).async
      .via(flattening).async
      .via(messageWrapping).async
      .to(sink)
  }
}
