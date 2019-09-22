package practice

object PatternMatching extends App {

  // Matching input
  def matchNumber(number: Int): String = number match {
    case 1 => "one"
    case 2 => "two"
    case _ => "other"
  }

  println(matchNumber(1)) // one
  println(matchNumber(100)) // other

  // Matching on Ty
  def matchType(x: Any): String = x match {
    case x: String => "String found"
    case x: Int => "Int found"
    case x: Boolean => "Boolean found"
    case x: Float => "Float found"
    case _ => "other"
  }

  println(matchType("String")) // String found
  println(matchType(190324)) // Int found
  println(matchType(true)) // Boolean found
  println(matchType(0.22)) // other
}
