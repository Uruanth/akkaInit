package part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object TimersSchedulers extends App {

  val system = ActorSystem("timers")


  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }


  //Se puede importar el contexto del scheduler, usar una variable implicita o enviar como argumento
  //  import system.dispatcher
  //
  //  val simpleActor = system.actorOf(Props[SimpleActor], "simple")
  //  system.log.info("Scheduling reminder for simpleActor")

  //  system.scheduler.scheduleOnce(1 second) {
  //    simpleActor ! "reminder"
  //  }(system.dispatcher)
  //
  //  private val routine = system.scheduler.scheduleWithFixedDelay(
  //    1 second,
  //    2 seconds,
  //    simpleActor,
  //    "reminder")
  //
  //
  //  system.scheduler.scheduleOnce(4 seconds){
  //    routine.cancel()
  //    system.terminate()
  //  }


  /**
   * Exercise: Implement a self-closing actor
   *
   *  - If the actor receives a messages (anything), you have 1 second to send it another message
   *  - If the time window expires, the actor will stop itself
   *  - If you send another message, the time window is reset
   */


  import system.dispatcher

//  class SelfClosingActor extends Actor with ActorLogging {
//
//    private var scheduler = createTimeoutWindow()
//
//    private def createTimeoutWindow(): Cancellable = {
//      context.system.scheduler.scheduleOnce(1 second) {
//        self ! "timeout"
//      }
//    }
//
//    override def receive: Receive = {
//      case "timeout" =>
//        log.info("Stopping myself")
//        context.stop(self)
//      case message =>
//        log.info(s"Received $message , staying alive")
//        scheduler.cancel()
//        scheduler = createTimeoutWindow()
//
//    }
//  }
//
//  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
//
//  system.scheduler.scheduleOnce(250 millis) {
//    selfClosingActor ! "ping"
//  }
//
//  system.scheduler.scheduleOnce(2 seconds) {
//    system.log.info("sending pong too the self-closing actor")
//    selfClosingActor ! "pong"
//  }

  /**
   * Timer
   */
  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {

    timers.startSingleTimer(TimerKey, Start, 500 millis)
    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startTimerWithFixedDelay(TimerKey, Reminder, 550 millis)
      case Reminder =>
        log.info("I'm alive")
      case Stop =>
        log.warning("Stopping!!")
        context.stop(self)
    }
  }


  val timerBasedHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerBasedHeartbeatActor ! Stop
    system.terminate()
  }
}
