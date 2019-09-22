package practice

object Generics extends App {

  // Example classes
  class Car
  class BMW extends Car
  class Tesla extends Car

  class Fruit
  class Banana extends Fruit

  class SomeGenericClass[A] {
    private var elements: List[A] = List()

    def add(x: A): Unit = elements = x :: elements
    def getLast: A = elements.head
  }

  val generic = new SomeGenericClass[Car]()
  generic.add(new Tesla)
  generic.add(new BMW)
  println(generic.getLast)

  // generic.add(new Banana) // Not possible
}
