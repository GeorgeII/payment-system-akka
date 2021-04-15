package actors

import akka.actor.{Actor, ActorLogging, ActorRef}

class PaymentParticipant extends Actor with ActorLogging {
  import PaymentParticipant._

  private[this] val startBalance = 100000L

  override def receive: Receive = onMessage(startBalance)

  private def onMessage(balance: Long): Receive = {
    case Payment(sign, value, anotherParticipant) =>
      sign match {
        case Minus =>
          if (value > balance) {
            anotherParticipant ! StopPayment
            log.warning(s"Payment aborted! Not enough money. Your balance is $$$balance but " +
              s"the withdrawal requires $$$value. Your bank account number: ${self.path.name}, " +
              s"another participant's account: ${anotherParticipant.path.name}")
          }
          else context.become(onMessage(balance - value))

        case Plus => context.become(onMessage(balance + value))
      }

    case StopPayment(value, anotherParticipant) =>
      context.become(onMessage(balance - value))
      log.warning(s"The payment from ${anotherParticipant.path.name} has been canceled. Another " +
        s"participant does not have enough money. $$$value was withdrawn.")

    case RequestBalance =>
      sender() ! ResponseBalance(balance)
  }
}

sealed trait PaymentSign

final case object Plus extends PaymentSign
final case object Minus extends PaymentSign

object PaymentParticipant {
  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef)
  case class StopPayment(value: Long, participant: ActorRef)

  case object RequestBalance
  case class ResponseBalance(balance: Long)
}
