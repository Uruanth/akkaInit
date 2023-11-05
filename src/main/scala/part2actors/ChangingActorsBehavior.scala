package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorsBehavior.Mom.{MomStart, MomStart2}

object ChangingActorsBehavior extends App {

  val system = ActorSystem("system")


  object FussyKid {
    case object KidAccept

    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {

    import FussyKid._
    import Mom._

    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {

    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      //Change to sadReceive when Food is vegetables
      case Food(VEGETABLE) =>
        println("[stateless] happy vege")
        context.become(sadReceive, false)
      case Food(CHOCOLATE) =>
        println("[stateless] happy choco")
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
        println("[stateless] sad vege")
      //Change to happyReceive when Food is Chocolate
      case Food(CHOCOLATE) =>
        println("[stateless] sad choco")
        context.become(happyReceive, false)
      case Ask(_) => sender() ! KidReject
    }


  }


  object Mom {

    case class MomStart(kidRef: ActorRef)
    case class MomStart2(kidRef: ActorRef)

    case class Food(food: String)

    case class Ask(message: String)

    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {

    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("do yoy want to play?")
      case MomStart2(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do yoy want to play?")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("My kid is sad")

    }
  }

  val fussyKid = system.actorOf(Props[FussyKid], "kid")
  val mom = system.actorOf(Props[Mom], "mom")
  val stataless = system.actorOf(Props[StatelessFussyKid], "staless")


//  mom ! MomStart(fussyKid)
  mom ! MomStart(stataless)
//  mom ! MomStart2(stataless)


  system.terminate()
}
