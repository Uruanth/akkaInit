package part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

import java.io.Console
import scala.sys.Prop

object MailBoxes extends App {

  val system = ActorSystem("MailboxesDemo", ConfigFactory.load()
    .getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * case #1 custom priority mailbox
   * P0 -> Most important
   * |
   * |
   * PN -> Non important
   */

  // step 1 - mailbox definition
  class SupportTicketPriorityMailBox(settings: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("P0") => 0
        case message: String if message.startsWith("P1") => 1
        case message: String if message.startsWith("P2") => 2
        case message: String if message.startsWith("P3") => 3
        case _ => 4
      }

    )

  /** step 2 - make it know in the config, application.conf
   *
   * en el mailbox-type esta la ruta de la carpeta y la clase a donde se usara
   *
   * #Mailboxes
   * support-ticket-dispatcher {
   * mailbox-type = "part5Infra.MailBoxes$SupportTicketPriorityMailBox"
   * }
   */

  // step 3 - attach the dispatscher to an actor

//  val supportTicetActor = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
//
//  supportTicetActor ! "P2 this thing would be nice to have"
//  supportTicetActor ! "P0 this needs to be solved NOW"
//  supportTicetActor ! "P1 do this when you have the time"


  /**
   * case #2 control-aware mailbox
   * we'll use UnboundedControlAwareMailbox
   */

  // step 1 - mark important messages as control messages
  case object ManagementTicket extends ControlMessage

  /*
   step 2 - configure who gets the mailbox
   - make the actor attach to the mailbox
   */

  //Method #1
  val controlAwareActor = system.actorOf(Props[SimpleActor]
    .withMailbox("control-mailbox"),
    "controlAwareActor"
  )

//  controlAwareActor ! "[P2] this thing would be nice to have"
//  controlAwareActor ! "[P0] this needs to be solved NOW"
//  controlAwareActor ! "[P1] do this when you have the time"
//  controlAwareActor ! ManagementTicket

  //Method #2 - Using deployment config
  val altControlAwareActor = system.actorOf(Props[SimpleActor]
    .withMailbox("control-mailbox"),
    "altControlAwareActor"
  )

  altControlAwareActor ! "[P2] this thing would be nice to have"
  altControlAwareActor ! "[P0] this needs to be solved NOW"
  altControlAwareActor ! "[P1] do this when you have the time"
  altControlAwareActor ! ManagementTicket


  system.terminate()

}
