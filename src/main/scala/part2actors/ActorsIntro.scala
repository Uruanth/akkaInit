package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  //part1 - actor system
  val actorSystem = ActorSystem("firtsActorSystem")

  //part2 - create actors
  class WordCountActor extends Actor {
    var totalWords = 0

    //Behavior
    def receive: Receive = {
      case message: String =>
        println(s"[Word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[Word counter] I cannot understand ${msg.toString}")
    }
  }


  //part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")

  //part4 - communicate
  wordCounter ! "I am learnig Akka and it's pretty damn cool"


  // Best practice for actors with arguments
  object Person {
    def props(name: String) = Props(new Person(name))
  }

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"hi my name is $name")
      case _ =>
    }
  }

  val person = actorSystem.actorOf(Person.props("bob"))
  person ! "hi"

   actorSystem.terminate()
}
