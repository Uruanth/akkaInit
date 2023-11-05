package part2actors

import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.event.Logging

object ActorLoggin extends App {

  val system = ActorSystem("logginActor")

  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message =>
        logger.info(message.toString)
    }
  }


  class ActorWithLoggin extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info("Interpolate log {}", message)
    }
  }


  val loggActor = system.actorOf(Props[SimpleActorWithExplicitLogger], "logActor")
  val loggActor2 = system.actorOf(Props[ActorWithLoggin], "logActor2")
  loggActor ! "foo"
  loggActor2 ! "foo2"
  system.terminate()


}
