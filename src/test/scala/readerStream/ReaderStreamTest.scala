package readerStream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import java.io.File
import scala.concurrent.duration.DurationInt

class ReaderStreamTest extends TestKit(ActorSystem("ReaderStreamTest"))
  with WordSpecLike
  with BeforeAndAfterAll {

  private[this] implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import ReaderStream._

  "getAllFilesFromFolder" should {
    "return a vector of all files" in {
      val files = getAllFilesFromFolder
      assert(files.nonEmpty)
      assert(files.length == 400)
    }
  }

  "readLinesInFile" should {
    /**
     * look at a generated file number of lines and compare this number minus 1 to
     * the method returning value length.
     */
    "return a Vector of n elements for a file with n lines" in {
      val file = new File("data/transactions-6.txt")
      val lines = readLinesInFile(file)
      assert(lines.length == 18763)
    }
  }

  "A sink actor" should {
    "receive at least 200,000 messages" in {
      val sinkActor = TestProbe("sinkActor")
      val graph = buildReadingStream(sinkActor.ref)
      graph.run()
      sinkActor.receiveN(200000, 5.second)
    }

    "receive a special message at the end when a stream completes successfully" in {
      val sinkActor = TestProbe("sinkActor")
      val graph = buildReadingStream(sinkActor.ref)
      graph.run()

      sinkActor.fishForSpecificMessage(15.second) {
        case OnCompleteMessage(msg) =>
          assert(msg == "The stream has completed successfully")
      }
    }
  }
}
