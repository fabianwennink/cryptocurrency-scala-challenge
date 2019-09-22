package practice

object Tuples extends App {

  // Regular Tuple, undefined size
  val product = ( "apple", 3 ) // name, quantity

  // Regular Tuple, defined size (4)
  // Tuples go from Tuple2 to Tuple22
  val fruit = Tuple4( "apple", "banana", "pineapple", "melon" )

  // Accessing the Tuple
  val ingredient = ( "sugar" , 100 )
  println(ingredient._1) // sugar
  println(ingredient._2) // 100

  // Accessing the Tuple (other way)
  val (name, quantity) = ingredient
  println(name) // sugar
  println(quantity) // 100
}
