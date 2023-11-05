package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorsBasicPractice.BankAccout.{Deposit, Statement, TransactionFailure, TransactionSuccess, Withdraw}
import part2actors.ActorsBasicPractice.Counter.{Decrement, Increment, Print}
import part2actors.ActorsBasicPractice.Person.LiveTheLife

object ActorsBasicPractice extends App {

  val system = ActorSystem("BasicPractice")

  object Counter {
    case object Increment

    case object Decrement

    case object Print
  }

  class Counter extends Actor {
    var count = 0

    override def receive: Receive = {
      case Increment => count + 1
      case Decrement => count + 1
      case Print => println(s"[Counter] My current count is $count")
    }
  }


  val counter = system.actorOf(Props[Counter], "counter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 5).foreach(_ => counter ! Decrement)

  counter ! Print


  object BankAccout {
    sealed trait CommandBank

    case class Deposit(amount: Int) extends CommandBank

    case class Withdraw(amount: Int) extends CommandBank

    case object Statement extends CommandBank

    case class TransactionSuccess(message: String) extends CommandBank

    case class TransactionFailure(reason: String) extends CommandBank
  }

  class BankAccout extends Actor {
    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid deposit amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"successfully deposited $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid withdraw amount")
        else if (amount > funds) sender() ! TransactionFailure("insuficient funds")
        else {
          funds -= amount
          sender() ! TransactionSuccess(s"successfully withdraw $amount")
        }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor{
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = system.actorOf(Props[BankAccout], "myAccount")
  val person = system.actorOf(Props[Person], "billonarie")

  person ! LiveTheLife(account)

  system.terminate()

}
