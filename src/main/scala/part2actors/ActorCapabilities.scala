package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    //Attribute from Actor
    //context
    //Ref self actor
    context.self


    private def printSome(typeC: String, foo: Any) = println(s"[Simple actor] I have received a $typeC: $foo")

    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hellom there!"
      case message: String => println(s"[Simple actor] I have received $message, I'm ${self.path.name} and sender is ${sender()}")
      case number: Int => println(s"[Simple actor] I have received a NUMBER: $number")
      case SpecialMessage(contents) => printSome("SpecialMessage", contents)
      case SendMessageToYourself(contents) =>
        //Use context.self like self, and send message to yourself
        self ! contents
        printSome("SendMessageToYourself", contents)
      case SayHiTo(ref) =>
        printSome(self.path.name, "sayTo")
        ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) =>
        println(s"self: $self and sender is ${sender()}")
        ref forward  (content + "")
      case _ =>
    }
  }

  val system = ActorSystem("System")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  //Messages can be of any type
//  simpleActor ! "Hello, actor"
//  simpleActor ! 2

  case class SpecialMessage(contents: String)

  case class SendMessageToYourself(contents: String)

  case class SayHiTo(ref: ActorRef)

  case class WirelessPhoneMessage(content: String, ref: ActorRef)

//  simpleActor ! SpecialMessage("Special message")
//  simpleActor ! SendMessageToYourself("self")


  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")
//
//  bob ! SayHiTo(alice)
  bob ! WirelessPhoneMessage("keep original sender", alice) //noSender

  system.terminate()

}
