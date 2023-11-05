package part1recap

object MultiThreadRecap extends App {

  val aThread = new Thread(() => println("I'm running in parallel"))
  aThread.start()
  aThread.join()

}
