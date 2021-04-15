package actors

import akka.actor.{Actor, ActorLogging, Props}

class PaymentChecker extends Actor with ActorLogging {
  import PaymentChecker._
  import LogIncorrectPayment._
  import readerStream.ReaderStream.OnCompleteMessage

  private val incorrectLoggingActor = context.actorOf(Props[LogIncorrectPayment])

  override def receive: Receive = {
    case CheckPayment(payment) =>
      if (isValid(payment)) sender() ! true// TODO: createParticipant
      else sender() ! false //incorrectLoggingActor ! IncorrectPayment(s"The $payment payment is invalid")

    case OnCompleteMessage(msg) => log.info(msg)
  }

  private[actors] def isValid(payment: String): Boolean = {
    val namePattern = "[a-zA-Z0-9]+".r
    val valuePattern = "[0-9]+".r
    val paymentPattern = s"$namePattern->$namePattern:$valuePattern".r
    payment.matches(paymentPattern.toString)

    // plan B in case the solution above won't work.
//    val paymentPattern = """[a-zA-Z0-9]+->[a-zA-Z0-9]+:[0-9]+""".r
//    paymentPattern.pattern.matcher(payment).matches


    // idk if Regular Expressions is a bad practice. So, in this case the below code can be used
    // (though, it needs to be tested and validated in a more sophisticated and accurate way
    // than Regexes above, e.g. two dashes or two colons can lead to a false positive
    // error).

//    val endOfFirstParticipant = payment.indexOf("-")
//    val firstParticipant = payment.substring(0, endOfFirstParticipant)
//
//    val startOfSecondParticipant = payment.indexOf(">")
//    val endOfSecondParticipant = payment.indexOf(":")
//    val secondParticipant = payment.substring(
//      startOfSecondParticipant + 1,
//      endOfSecondParticipant
//    )
//
//    val value = payment.substring(endOfSecondParticipant + 1)
//
//    // check whether every part is valid by a given specification.
//    firstParticipant.forall(_.isLetterOrDigit) &&
//      secondParticipant.forall(_.isLetterOrDigit) &&
//      value.forall(_.isDigit)
  }
}

object PaymentChecker {
  case class CheckPayment(payment: String)
}
