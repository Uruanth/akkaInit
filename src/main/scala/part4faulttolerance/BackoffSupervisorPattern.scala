package part4faulttolerance

import akka.actor.SupervisorStrategy.{Stop, stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import java.io.File
import scala.io.Source
import scala.concurrent.duration._
import scala.language.postfixOps

object BackoffSupervisorPattern extends App {

  val system = ActorSystem("backoff")

  case object ReadFile

  class FileBasedPersistendActor extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit =
      log.info(s"Persistent actor starting ${self.path.name}")

    override def postStop(): Unit =
      log.warning(s"Persistent actor has stopped  ${self.path.name}")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.warning("Persistent actor restarting")

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
          dataSource = Source.fromFile(new File("src/main/resources/test/files/important_fail.txt"))
        log.info("I've just read some IMPORTANT data: " + dataSource.getLines().toList)
    }
  }


  //  private val simpleActor = system.actorOf(Props[FileBasedPersistendActor], "readActor")

  //  simpleActor ! ReadFile
//
//  private val simpleSupervisorProps = BackoffSupervisor.props(
//    BackoffOpts.onFailure(
//      Props[FileBasedPersistendActor],
//      "simpleBackoffActor",
//      3 seconds,
//      30 seconds,
//      0.2
//    )
//  )
//
//
//  private val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
//  simpleBackoffSupervisor ! ReadFile
//
//
//  private val stopSupervisorProps = BackoffSupervisor.props(
//    BackoffOpts.onStop(
//      Props[FileBasedPersistendActor],
//      "stopBackoffActor",
//      3 seconds,
//      30 seconds,
//      0.2
//    ).withSupervisorStrategy(
//      OneForOneStrategy() {
//        case _ => Stop
//      }
//    )
//  )

//  val stopSupervisor = system.actorOf(stopSupervisorProps, "stopSupervisor")
//
//  stopSupervisor ! ReadFile



  class EagerFBPActor extends FileBasedPersistendActor {
    override def preStart(): Unit = {
      log.info("Eager actor starting")
     dataSource = Source.fromFile(new File("src/main/resources/test/files/important_fail.txt"))
    }
  }



  val eagerActorProps = BackoffSupervisor.props(
    BackoffOpts.onStop(
      Props[EagerFBPActor],
      "eagerActor",
      1 second,
      10 second,
      0.2
    )
  )

  val eagerActor = system.actorOf(eagerActorProps)
  eagerActor ! ReadFile

//  system.terminate()

}
