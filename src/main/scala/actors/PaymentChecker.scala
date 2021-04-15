package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class PaymentChecker extends Actor with ActorLogging {
  import PaymentChecker._
  import LogIncorrectPayment._
  import PaymentParticipant._
  import readerStream.ReaderStream.OnCompleteMessage

  private val incorrectLoggingActor = context.actorOf(Props[LogIncorrectPayment], "invalidLogger")

  override def receive: Receive = {
    case CheckPayment(payment) =>
      if (isValid(payment)) {
        val (firstParticipant, secondParticipant, value) = extractParticipantsAndValue(payment)
        val firstParticActor  = searchOrCreateActor(firstParticipant)
        val secondParticActor = searchOrCreateActor(secondParticipant)

        firstParticActor  ! Payment(Minus, value.toLong, secondParticActor)
        secondParticActor ! Payment(Plus, value.toLong, firstParticActor)

      }
      else incorrectLoggingActor ! IncorrectPayment(s"The $payment payment is invalid")

    case OnCompleteMessage(msg) => log.info(msg)
  }

  private[actors] def searchOrCreateActor(actorName: String): ActorRef = {
    val childActor = context.child(actorName)

    childActor.getOrElse {
      context.actorOf(Props[PaymentParticipant], name = actorName)
    }
  }

  /**
   * Important: the payment should be valid in terms of syntax.
   * @param payment - participant1->participant2:value type of string
   * @return - (participant1, participant2, value)
   */
  private[actors] def extractParticipantsAndValue(payment: String): (String, String, String) = {
    val endOfFirstParticipant = payment.indexOf("-")
    val firstParticipant = payment.substring(0, endOfFirstParticipant)

    val startOfSecondParticipant = payment.indexOf(">")
    val endOfSecondParticipant = payment.indexOf(":")
    val secondParticipant = payment.substring(
      startOfSecondParticipant + 1,
      endOfSecondParticipant
    )

    val value = payment.substring(endOfSecondParticipant + 1)

    (firstParticipant, secondParticipant, value)
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
