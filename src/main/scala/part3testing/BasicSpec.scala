package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random


class BasicSpec extends TestKit(ActorSystem("BasicTest"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  import BasicSpec._

  "A simpleActor" should {
    "sen back de the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello test"
      echoActor ! message

      expectMsg(message)

    }

  }
  "A blackhole actor" should {
    "sen back some message" in {
      val echoActor = system.actorOf(Props[BlackHole])
      val message = "hello test"
      echoActor ! message

      expectNoMessage(1 second)

    }
  }

  "A lab test Actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into uppercase" in {

      labTestActor ! "I love akka"

      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
      //      expectMsg("I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply to a favorite" in {
      labTestActor ! "favorite"
      expectMsgAllOf("scala", "akka")
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favorite"
      val messages = receiveN(2)
      assert(messages.length == 2)
    }

  }

}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val rm = new Random()

    override def receive: Receive = {
      case "greeting" =>
        if (rm.nextBoolean()) sender() ! "hi"
        else sender() ! "hello"

      case "favorite" =>
        sender() ! "scala"
        sender() ! "akka"

      case message: String => sender() ! message.toUpperCase()
    }
  }

}