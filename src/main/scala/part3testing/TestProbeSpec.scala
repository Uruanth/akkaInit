package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.language.postfixOps

class TestProbeSpec extends TestKit(ActorSystem("BasicTest"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

    }

    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoad = "Tester"
      master ! Work(workLoad)

      slave.expectMsg(SlaveWork(workLoad, testActor))
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3))
      //      expectMsgAllOf(RegistrationAck, Report(3))
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoad = "Tester"
      master ! Work(workLoad)
      master ! Work(workLoad)


      slave.receiveWhile() {
        case SlaveWork(`workLoad`, `testActor`) =>
          slave.reply(WorkCompleted(3, testActor))
      }
      expectMsgAllOf(Report(3), Report(6))
    }

  }

}

object TestProbeSpec {

  case object RegistrationAck

  case class Register(ref: ActorRef)

  case class Work(text: String)

  case class WorkCompleted(count: Int, originalSen: ActorRef)

  case class SlaveWork(text: String, originalRequester: ActorRef)

  case class Report(totalCount: Int)

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ =>
    }

    def online(ref: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) =>
        println("in work")
        ref ! SlaveWork(text, sender())
      case WorkCompleted(count, originalSen) =>
        val newTotalWordCount = totalWordCount + count
        println(s"$newTotalWordCount en completed")
        originalSen ! Report(newTotalWordCount)
        context.become(online(ref, newTotalWordCount))
      case _ =>
    }

  }
}
