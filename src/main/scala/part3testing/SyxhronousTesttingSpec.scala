package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.Duration


class SyxhronousTesttingSpec
extends AnyWordSpecLike
with BeforeAndAfterAll {

implicit val system = ActorSystem("Synchronous")

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  import SyxhronousTesttingSpec._
  "A counter" should {
    "synchronously increse its counter" in {
      val actor = TestActorRef[Counter](Props[Counter])(system)
      actor ! Inc
      assert(actor.underlyingActor.count==1)
    }

    "synchronously increase its counter at the call of the receive function" in {
      val actor = TestActorRef[Counter](Props[Counter])
      actor.receive(Inc)
      assert(actor.underlyingActor.count==1)
    }

    "work on the calling thread dispatcher" in {
      val counter = system
        .actorOf(Props[Counter]
        .withDispatcher(CallingThreadDispatcher.Id))

      val probe = TestProbe()
      probe.send(counter, Read)
      probe.expectMsg(Duration.Zero, 0)

    }

  }

}

object SyxhronousTesttingSpec {

  case object Inc
  case object Read

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }
  }

}
