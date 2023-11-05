package part1recap

object AdvanceRecap extends App {

  // partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 22
    case 5 => 2
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 22
    case 5 => 2
  }

}
