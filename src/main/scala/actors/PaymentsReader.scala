package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.ActorMaterializer

class PaymentsReader(incorrectLoggingActor: ActorRef) extends Actor {
  import PaymentsReader._
  import readerStream.ReaderStream.buildReadingStream

  implicit val materializer = ActorMaterializer()

  val checkerActor: ActorRef =
    context.actorOf(Props(new PaymentChecker(incorrectLoggingActor)), "checker")

  override def receive: Receive = {
    case Start =>
      val readerStream = buildReadingStream(checkerActor)
      readerStream.run()
  }
}

object PaymentsReader {
  case object Start
}
