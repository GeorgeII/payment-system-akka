import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.io.{Source => ScalaFileSource}

class ReaderStream {

  val config = ConfigFactory.load("application.conf")
  val configField = "payments-folder"

  private def getAllFilesFromFolder: Vector[File] = {
    val filesFolder = new File(config.getString(configField))

    if (filesFolder.exists && filesFolder.isDirectory) {
      filesFolder.listFiles.filter(_.isFile).toVector
    }
    else {
      Vector[File]()
    }
  }

  private def readLinesInFile(file: File): Vector[String] = {
    val bufferedFileReader = ScalaFileSource.fromFile(file)
    val lines = bufferedFileReader.getLines.toVector
    bufferedFileReader.close()

    lines
  }

  def buildReadingStream(sinkActor: ActorRef): RunnableGraph[NotUsed] = {
    import ReaderStream.OnCompleteMessage
    
    val sourceFiles = Source(getAllFilesFromFolder)
    val linesFromFile = Flow[File].map(file => readLinesInFile(file))
    val flattening = Flow[Vector[String]].mapConcat(identity)
    val sink = Sink.actorRef(
      sinkActor,
      OnCompleteMessage("The stream has completed successfully")
    )

    sourceFiles
      .via(linesFromFile)
      .via(flattening)
      .to(sink)
  }
}

object ReaderStream {
  case class OnCompleteMessage(msg: String)
}
