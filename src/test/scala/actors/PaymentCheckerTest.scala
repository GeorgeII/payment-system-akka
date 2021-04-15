package actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class PaymentCheckerTest extends TestKit(ActorSystem("PaymentCheckerTest"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import PaymentChecker._

  "isValid" should {
    "return false for an invalid payment" in {
      val actor = system.actorOf(Props[PaymentChecker])
      actor ! CheckPayment("8ewtf9weRE2->Thrcxv5@l:15343")
      expectMsg(false)
    }

    "return true for a valid payment" in {
      val actor = system.actorOf(Props[PaymentChecker])
      actor ! CheckPayment("8ewtf9weRE2->Thrcxv5l:15343")
      expectMsg(true)
    }

    "return false for multiple invalid payments" in {
      val invalidPayments = List(
        CheckPayment("<#dw>^->N>:7wyZfRX"),
        CheckPayment("&;doKU[U+->TgW%G(U%VFEj:9iSAuel"),
        CheckPayment("9aPeD6\\lZmt4cB->&lc:BvxiGmQ"),
        CheckPayment("Nf<->KAW0Q@<(AC:BsP:Z8YZt0a"),
        CheckPayment("asd123->qwer543:23i1"),
        CheckPayment("qwe333->SLD!G4:154294"),
        CheckPayment("3->1:1 2"),
        CheckPayment("vgd.re->asd14K:12345)")
      )
      val actor = system.actorOf(Props[PaymentChecker])
      invalidPayments.foreach { payment =>
        actor ! payment
        expectMsg(false)
      }
    }

    "return true for multiple valid payments" in {
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
      val actor = system.actorOf(Props[PaymentChecker])
      validPayments.foreach { payment =>
        actor ! payment
        expectMsg(true)
      }
    }
  }
}
