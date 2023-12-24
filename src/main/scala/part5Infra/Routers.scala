package part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

object Routers extends App {

  //Take config from file application.conf
  val system = ActorSystem("routerSystem", ConfigFactory.load().getConfig("routerDemo"))
  //  val system = ActorSystem("routerSystem")

  /**
   * #1 - Manual Router
   */
  class Master extends Actor {
    // Step 1 -> create routes
    // 5 actor routes based of Slave actors
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    // Step 2 -> define router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = onMessage(router)

    private def onMessage(router: Router): Receive = {
      //Step 4 -> handle the termination/lifecycle of the routes
      case Terminated(ref) =>
        context.become(onMessage(router.removeRoutee(ref)))
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        context.become(onMessage(router.addRoutee(newSlave)))

      //Step 3-> route the messages
      case message =>
        router.route(message, sender())

    }
  }

  private class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"${message.toString} - ${self.path.name}")
    }
  }


  private val actorMaster = system.actorOf(Props[Master], "actorMaster")

  //  for(i <- 1 to 10){
  //    actorMaster ! s"${i} Hello from the world"
  //  }


  /**
   * Method 2 -> a router actor with it own children
   * POOL router
   */


  //In code
  //  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
  //  for (i <- 1 to 10){
  //    poolMaster ! s"$i Hello from the world"
  //  }

  //from configuration file
  //  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
  //    for (i <- 1 to 10){
  //      poolMaster2 ! s"$i Hello from the world"
  //    }

  /**
   * Method #3 -> router with actors creater
   */

    val slaveList = (1 to 3)
      .map(i => system.actorOf(Props[Slave], s"slave_$i"))
      .toList
  //
//    val slavePaths = slaveList
//      .map(slaveRef => slaveRef.path.toString)

//    slavePaths.foreach(println)
  //  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
  //  for (i <- 1 to 10) {
  //    groupMaster ! s"$i Hello from the world"
  //  }

  /**
   * From config file
   * Here take actors created in slaveList and use
   */
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
  for (i <- 1 to 10) {
    groupMaster2 ! s"$i Hello from the world"
  }


  /**
   * Special messages
   * Broadcast is message that received everyone
   */

  groupMaster2 ! Broadcast("hello, everyone")



  system.terminate()

}
