package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object VotesScenario extends App {

  /**
   * A simplified voting system
   */

  case class Vote(candidate: String)

  case object VoteStatusRequest

  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(c) =>
        println("init")
        context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest =>
        println("send")
        sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {
    override def receive: Receive = awaitingComman

    def awaitingComman: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(citizesRef => citizesRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))

    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator poll stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }

  }


  val system = ActorSystem("System")

  val alice2 = system.actorOf(Props[Citizen])
  val bob2 = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice2 ! Vote("Martin")
//  bob2 ! Vote("Rob")
//  charlie ! Vote("Al")
//  daniel ! Vote("Martin")
//
  val voteAggregator = system.actorOf(Props[VoteAggregator])
//  voteAggregator ! AggregateVotes(Set(alice2, bob2, charlie, daniel))
  voteAggregator ! AggregateVotes(Set(alice2))

  system.terminate()
}