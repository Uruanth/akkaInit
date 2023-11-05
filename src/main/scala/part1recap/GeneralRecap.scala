package part1recap

object GeneralRecap extends App {

  val aCodeBlock = {
    if (true) 34
    55
  }

  val pairs = for{
    char <- List('a','b','c','d')
    num <- List(1,2,3)
  } yield num + "-" + char


  println(pairs)
}
