import akka.actor.{ActorSystem, Props}

import actors.PaymentsReader
import actors.PaymentsReader.Start

object Application {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("paymentSystem")
    val launcher = system.actorOf(Props[PaymentsReader], "application")

    launcher ! Start
  }
}
