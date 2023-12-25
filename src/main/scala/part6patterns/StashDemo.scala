package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /*
  ResourceActor
    - open => it can receive read/write request to the resource
    - otherwise it will pospone all read/write request until the state is open
  ResourceActor is closed
    - Open => switch to the open state
    - Read, Write messages are POSTPONED

  ResourceActor is open
    - Read, Write are handled
    - Close => switch to the Close state
  */

  case object Open

  case object Close

  case object Read

  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    private def closed: Receive = {
      case Open =>
        log.info("opening resource ")
        // step 3 - unstashall when you switch the message handler
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the closed state")
        // step 2 - stash away what you can't handle
        stash()
    }

    private def open: Receive = {
      case Read =>
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I'm writing $data")
        innerData = data
      case Close =>
        log.info("closing resource")
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the open state")
        stash()
    }
  }


  val system = ActorSystem("Stashing")
  val resourceActor = system.actorOf(Props[ResourceActor], "resourceActor")

  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Open
  resourceActor ! Write("I love stash")
  resourceActor ! Close
  resourceActor ! Read


  system.terminate()
}
