import akka.actor.ActorRef
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory

import java.io.File
import scala.io.{Source => FileSource}

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
    val bufferedFileReader = FileSource.fromFile(file)
    val lines = bufferedFileReader.getLines.toVector
    bufferedFileReader.close()

    lines
  }

  private def buildReadingStream(sinkActor: ActorRef): Unit = {
    val files = getAllFilesFromFolder

    val sourceFiles = Source(files)
    val readLinesInFile = Flow[File].map(file => readLinesInFile(file))
    val flattening = Flow[Vector[String]].mapConcat(identity)
//    val sink = Sink.actorRef(sinkActor)

    sourceFiles.via(readLinesInFile).via(flattening).to(sink)
  }

}
