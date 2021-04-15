package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer

class PaymentsReader extends Actor {
  import PaymentsReader._
  import readerStream.ReaderStream.buildReadingStream

  implicit val materializer = ActorMaterializer()

  val checkerActor: ActorRef = context.actorOf(Props[PaymentChecker])

  override def receive: Receive = {
    case Start =>
      val readerStream = buildReadingStream(checkerActor)
      readerStream.run()
  }
}

object PaymentsReader {
  case object Start
}
