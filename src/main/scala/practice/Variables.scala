package practice

object Variables extends App {

  // Mutable variable
  var name = "Fabian"
  name = "SomethingElse"

  // Immutable variable
  val lastName = "Wennink"
  // lastname = "OtherLastName" // Not possible

  val typedVariable: String = "This is a string"
  // typedVariable = 100 // Not possible
}
