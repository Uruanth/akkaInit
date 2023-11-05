package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifecycle extends App {
  val system = ActorSystem("LifeCycle")

  object StartChild

  //  class LifecicleActor extends Actor with ActorLogging {
  //
  //    override def preStart(): Unit = log.info(s"I am starting $self")
  //    override def postStop(): Unit = log.info(s"I have stopped $self")
  //
  //    override def receive: Receive = {
  //      case StartChild =>
  //        context.actorOf(Props[LifecicleActor], "child")
  //    }
  //  }
  //
  //
  //  val parent = system.actorOf(Props[LifecicleActor], "parent")
  //  parent ! StartChild
  //  parent ! PoisonPill

  /**
   * Restart
   */
  object Fail

  object FailChild

  object CheckChild

  object Check

  class Parent extends Actor {
    val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild =>
        child ! Fail
      case CheckChild =>
        child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info(s" $self I am starting ")

    override def postStop(): Unit = log.info(s"$self I have stopped ")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervisedActor restarting be of ${reason.getMessage} ")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")


    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")
      case Check =>
        log.info(s"check alive and kicking $self")
    }
  }

  val supervised = system.actorOf(Props[Parent], "parent")
  supervised ! FailChild
  supervised ! CheckChild

  system.terminate()

}
