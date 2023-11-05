package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

class SimpleActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case message => log.info(message.toString)
  }
}


  val configString =
    """
      | akka {
      |   loglevel = "DEBUG"
      |}
      |""".stripMargin


  val config = ConfigFactory.parseString(configString)

  val system = ActorSystem("system", ConfigFactory.load(config))
  val actorl = system.actorOf(Props[SimpleActor],"actor")

  actorl ! "simple message"


  /**
   * Configuracion diferente a akka en el archivo application.conf
   */
  val specialConfig = ConfigFactory.load().getConfig("customConfig")
  val specialConfigSystem = ActorSystem("SpecialConf", specialConfig)

  /**
   * Configuracion diferente a archivo que el application.conf
//   */
  val separeteConf = ConfigFactory.load("dummyConf.conf")
  println(s"separete log level ${separeteConf.getString("akka.loglevel")}")

  system.terminate()
}
