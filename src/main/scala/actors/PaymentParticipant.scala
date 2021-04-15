package actors

import akka.actor.{Actor, ActorRef}

class PaymentParticipant extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}

sealed trait PaymentSign

final case object Plus extends PaymentSign
final case object Minus extends PaymentSign

object PaymentParticipant {
  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef)
  case class StopPayment(message: String)
}
