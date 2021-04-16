import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationInt

class ApplicationTest extends TestKit(ActorSystem("ApplicationTest"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import actors.PaymentChecker.{RequestIsCompleted, ResponseIsCompleted}
  import actors.PaymentsReader.Start
  import actors.{LogIncorrectPayment, PaymentsReader}

  "An app" should {

    /**
     * Before this test you have to change 'payments-folder' field to 'data/test' in the config.
     */
    "complete within 10 seconds on test data" in {
      val invalidPaymentsLogger = system.actorOf(
        Props[LogIncorrectPayment],
        "invalidPaymentLogger"
      )
      val launcher = system.actorOf(
        Props(new PaymentsReader(invalidPaymentsLogger)),
        "application"
      )

      launcher ! Start

      import system.dispatcher

      system.scheduler.scheduleOnce(5.second) {
        launcher ! RequestIsCompleted
      }

      expectMsg(7.second, ResponseIsCompleted(true))
    }
  }
}
