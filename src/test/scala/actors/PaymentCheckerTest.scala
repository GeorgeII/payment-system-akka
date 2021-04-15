package actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationInt

class PaymentCheckerTest extends TestKit(ActorSystem("PaymentCheckerTest"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import PaymentChecker._
  import LogIncorrectPayment.IncorrectPayment

  "isValid" should {

    val loggerProbe = TestProbe("loggerProbe")
    val checker = system.actorOf(Props(new PaymentChecker(loggerProbe.ref)), "checker")

    "send an invalid payment to logging actor" in {
      val payment = "8ewtf9weRE2->Thrcxv5@l:15343"
      checker ! CheckPayment(payment)
      loggerProbe.expectMsg(IncorrectPayment(s"The $payment payment is invalid"))
    }

    "not send a valid payment to logging actor" in {
      checker ! CheckPayment("ewtf9weRE2->Thrcxv5l:15343")
      loggerProbe.expectNoMessage(2.second)
    }

    "send all invalid payments to logging actor" in {
      val invalidPayments = List(
        CheckPayment("<#dw>^->N>:7wyZfRX"),
        CheckPayment("&;doKU[U+->TgW%G(U%VFEj:9iSAuel"),
        CheckPayment("9aPeD6lZmt4cB->&lc:BvxiGmQ"),
        CheckPayment("Nf<->KAW0Q@<(AC:BsP:Z8YZt0a"),
        CheckPayment("asd123->qwer543:23i1"),
        CheckPayment("qwe333->SLD!G4:154294"),
        CheckPayment("3->1:1 2"),
        CheckPayment("vgd.re->asd14K:12345)")
      )
      invalidPayments.foreach { payment =>
        checker ! payment
      }

      loggerProbe.receiveN(invalidPayments.length, 1.second)
    }

    "not send a single valid message to logging actor" in {
      val validPayments = List(
        CheckPayment("asd2->qwe1:123"),
        CheckPayment("FzKeW2Jha8xh->S7ayXVSe:541165"),
        CheckPayment("wUyW7->y3u91vlV7y6rdNt:42124"),
        CheckPayment("98OC7I2EzCvZ1zf->EYxY7T:924897"),
        CheckPayment("dvZ3->Dj1:127869"),
        CheckPayment("N8WT32CtISwr->zMWbRUFAKca:417213"),
        CheckPayment("OCH->WM:268301"),
        CheckPayment("a->b:1"),
        CheckPayment("3->1:1"),
        CheckPayment("EbnsRnT1Erh->1KNxyiU0OMdbEN:518597")
      )
      validPayments.foreach { payment =>
        checker ! payment
      }

      loggerProbe.expectNoMessage(1.second)
    }
  }
}
