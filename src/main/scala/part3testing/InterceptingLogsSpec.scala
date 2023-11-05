package part3testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll}

class InterceptingLogsSpec extends
  TestKit(ActorSystem(
    "BasicTest",
    ConfigFactory
      .load()
      .getConfig("interceptingLogMessages")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogsSpec._

  val item = "Rock the JVM"
  val credicard = "12343-423-412412-412421-123"

  "A checkout flow" should {

    "correctly log dispath of an order" in {
      //Busca log a nivel log.info
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched",
        occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, credicard)
      }
    }
  }

}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)

  case class AuthorizeCard(creditCard: String)

  case class DispatchOrder(item: String)

  case object PaymentAccepterd

  case object PaymentDenied

  case object OrderConfirmed

  class CheckoutActor extends Actor {

    private val paymentManager = context.actorOf(Props[PaymentManaget])
    private val fullfillmentManager = context.actorOf(Props[FullfillmentManager])

    override def receive: Receive = awaiting

    def pendingFullfillment(item: String): Receive = {
      case OrderConfirmed =>
        context.become(awaiting)
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepterd =>
        fullfillmentManager ! DispatchOrder(item)
        context.become(pendingFullfillment(item))
      case PaymentDenied =>
        println("denied order")
    }

    def awaiting: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }

  }

  class PaymentManaget extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if (card.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepterd
    }
  }

  class FullfillmentManager extends Actor with ActorLogging{
    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched")
        sender()  ! OrderConfirmed

    }
  }

}
