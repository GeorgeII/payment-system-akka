import akka.actor.{ActorSystem, Props}
import actors.{LogIncorrectPayment, PaymentsReader}
import actors.PaymentsReader.Start

object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("paymentSystem")

    val invalidPaymentsLogger = system.actorOf(
      Props[LogIncorrectPayment],
      "invalidPaymentLogger"
    )

    val launcher = system.actorOf(
      Props(new PaymentsReader(invalidPaymentsLogger)),
      "application"
    )

    launcher ! Start
  }
}
