package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  val system = ActorSystem("system")


  object WordCounterMaster {
    case class Initialize(nChildern: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCounterReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildern) =>
        val childrenRefs = for (i <- 1 to nChildern) yield context.actorOf(Props[WordCounterWorker], s"child${i}")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(
                      childrenRefs: Seq[ActorRef],
                      currentIndexChild: Int,
                      currentTaskid: Int,
                      requestMap: Map[Int, ActorRef]
                    ): Receive = {

      case text: String =>
        println(s"[master] I have receiver $text - I will send it to child $currentIndexChild")
        val originalSender = sender()
        val task = WordCountTask(currentTaskid, text)
        val chidlRef = childrenRefs(currentIndexChild)
        chidlRef ! task
        val nextChild = (currentIndexChild + 1) % childrenRefs.length
        val newTaskId = currentTaskid + 1
        val newRequestMap = requestMap + {
          currentTaskid -> originalSender
        }
        context.become(withChildren(childrenRefs, nextChild, newTaskId, newRequestMap))

      case WordCounterReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentIndexChild, currentTaskid, requestMap - id))

    }


  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have receiber task $id with $text")
        sender() ! WordCounterReply(
          id,
          text.split(" ").length
        )
    }
  }


  class TestActor extends Actor{
    import WordCounterMaster._
    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List(
          "Lorem ipsum dolor sit amet",
          "Consectetur adipiscing elit",
          "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
          "Ut enim ad minim veniam",
          "Quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat",
          "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur",
          "Excepteur sint occaecat cupidatat non proident",
          "Sunt in culpa qui officia deserunt mollit anim id est laborum"
        )

        texts.foreach(text => master ! text)

      case count: Int =>
        println(s"[test actor] I received a replay $count")
    }
  }

  val test = system.actorOf(Props[TestActor], "childs")

  test ! "go"

  system.terminate()
}
