package practice

object Implicit extends App {

  implicit val prefix: String = "Hello "
  def prefix(s: String)(implicit prefix: String): String = prefix ++ s

  println(prefix("Fabian")) // Hello Fabian
}
