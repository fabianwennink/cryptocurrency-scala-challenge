package practice

object Lists extends App {

  // List of Strings
  val fruit: List[String] = List("apples", "oranges", "pears")

  // List of Integers
  val numbers: List[Int] = List(10, 20, 30, 40, 50)

  // Empty List
  val empty: List[Nothing] = List()

  // Two dimensional List
  val dim: List[List[Int]] =
    List(
      List(1, 0, 1),
      List(0, 1, 0),
      List(1, 0, 1)
    )

  // Any type List
  var any: List[Any] = List("first", "second", 1, 2, 3.14, true, false)

  // Combining Lists
  var combined: List[Any] = fruit ++ numbers

  // Fill List 5x with apples
  val fruitFill = List.fill(5)("apples")
}
