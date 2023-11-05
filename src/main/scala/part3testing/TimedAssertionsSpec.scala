package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class TimedAssertionsSpec extends
  TestKit(ActorSystem("BasicTest"))
  //Con configuracion
//  TestKit(ActorSystem("BasicTest", ConfigFactory.load()))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  import TimedAssertionsSpec._

  "A worker actor" should {
    val workeActor = system.actorOf(Props[WorkerActor])

    "reply with the meaning of life in a timely manner" in {

      /**
       * Primer valor el minimo que debe durar
       * Segundo, el maximo que puede durar
       * si se deja un solo valor, debe ser lo maximo que debe durar
       */
      within(500 millis, 1 second) {
        workeActor ! "work"
        expectMsg(WorkResult(42))
      }
    }


    "reply withvalid work at a reasonable cadence" in {

      workeActor ! "workSequence"

      /**
       * max: Duracion maxima
       * idle: duracion maxima entre mensajes
       * messages: cantidad maxima de mensajes
       */
      val results: Seq[Int] = receiveWhile[Int](max=2 seconds, idle=500 millis, messages = 10){
        case WorkResult(result) => result
      }

      assert(results.sum > 5)

    }

    "reply to a test probe in a timely manner" in {
      within(500 millis, 1 second) {
        val probe = TestProbe()
        probe.send(workeActor, "work")
        probe.expectMsg(WorkResult(42))
      }
    }

  }
}

object TimedAssertionsSpec {
  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender() ! WorkResult(42)
      case "workSequence" =>
        val r = new Random()
        for (i <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }

}
