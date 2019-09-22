package practice

object ForLoops extends App{

  val numbers: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // Simple for loop, prints 1 to 5
  for(number <- 1 to 5){
    println(s"1. Number = $number")
  }

  // Simple for loop, prints 1 to 4 (excludes last one 5)
  for(number <- 1 until 5){
    println(s"2. Number = $number")
  }

  // Simple for loop with a filter
  for(number <- numbers if number == 3){
    println(s"3. Number = $number")
  }

  // For loop with yield
  val correctNumber = for {
    number <- numbers
    if (number == 5 || number == 8)
  } yield number
  println(s"4. Number = $correctNumber")

  // Foreach loop, using the foreach function
  numbers.foreach(println(_))
}
