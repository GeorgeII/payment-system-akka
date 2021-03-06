package actors

import akka.actor.{Actor, ActorLogging}

class LogIncorrectPayment extends Actor with ActorLogging {
  import LogIncorrectPayment._

  override def receive: Receive = {
    case IncorrectPayment(msg) =>
      log.info(s"Incorrect payment: $msg")
  }
}

object LogIncorrectPayment {
  case class IncorrectPayment(message: String)
}
