package part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatchers extends App {


  class Counter extends Actor with ActorLogging {

    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"${self.path.name} [$count] $message")
    }
  }

  val actorSystem = ActorSystem("DispatcherDemo") //, ConfigFactory.load().getConfig("my-dispatcher")

  //Method 1 programmatic / in code
  val actors = for (i <- 1 to 10) yield
    actorSystem.actorOf(Props[Counter]
      .withDispatcher("my-dispatcher"),
      s"simpleCounter_$i")


  val r = new Random()
  /**
   * Send message default dispatcher
   */
  //  for(i<- 1 to 1000){
//    actors(r.nextInt(10)) ! i
//  }


  // method 2 from config
  val rtjvmActor = actorSystem.actorOf(Props[Counter], "rtjvm")





  class DBActor extends Actor with ActorLogging {

    implicit val executionContext: ExecutionContext = context.dispatcher
//    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")

    override def receive: Receive = {
      case message => Future {
        //Wait on resource
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }


  val dbActor = actorSystem.actorOf(Props[DBActor])
//  dbActor ! "the meaning of life is 42"


  val nonBlockingActor = actorSystem.actorOf(Props[Counter])

  for(it <- 1 to 1000){
    val message = s"important message $it"
    dbActor ! message
//    nonBlockingActor ! message
  }



//  actorSystem
//    .terminate()
}