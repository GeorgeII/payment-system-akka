import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.config.ConfigFactory

class PaymentsReader extends Actor {
  import PaymentsReader._

  val config = ConfigFactory.load("application.conf")
  val configField = "payments-folder"

  val checkerActor = context.actorOf(Props[PaymentChecker])

  override def receive: Receive = {
    case Start =>

  }
}

object PaymentsReader {
  case object Start
}
