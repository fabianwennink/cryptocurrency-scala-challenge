package practice

object Classes extends App {

  // Simple class definition
  class User(name: String, age: Int)

  // Regular class
  class Storage() {
    private var users: List[User] = List()

    def addUser(user: User): Unit = {}
    def removeUser(user: User): Unit = {}
  }

  // Regular class with private members
  class BankAccount() {
    private var _amount = 0

    def amount_= (value: Int): Unit = {
      _amount = value
    }
  }

  val account = new BankAccount()
  account.amount_=(100)
}
