import akka.actor.{Actor, Props}

class PaymentChecker extends Actor {
  import PaymentChecker._
  import LogIncorrectPayment._

  private val incorrectLoggingActor = context.actorOf(Props[LogIncorrectPayment])

  override def receive: Receive = {
    case CheckPayment(payment) =>
//      if (isValid(payment)) // TODO: createParticipant
//      else incorrectLoggingActor ! IncorrectPayment(s"The $payment payment is invalid")
  }

  private def isValid(payment: String): Boolean = {
    val namePattern = "[a-zA-Z0-9]".r
    val valuePattern = "[0-9]".r
    payment.matches(s"$namePattern->$namePattern:$valuePattern")


    // idk if Regular Expressions is a bad practice. So, in this case the below code can be used
    // (though, it needs to be tested and validated in a more sophisticated and accurate way
    // than pattern matching above, e.g. two dashes or two colons can lead to a false positive
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
