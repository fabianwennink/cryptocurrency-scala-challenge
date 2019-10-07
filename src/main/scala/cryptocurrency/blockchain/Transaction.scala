package cryptocurrency.blockchain

import cryptocurrency.network.JsonProtocol
import spray.json._

case class Wallet(address: String)
case class Transaction(sender: Wallet, receiver: Wallet, amount: Int, timestamp: Long = -1)

