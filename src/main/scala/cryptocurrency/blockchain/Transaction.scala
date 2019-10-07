package cryptocurrency.blockchain

case class Wallet(address: String)
case class Transaction(sender: Wallet, receiver: Wallet, amount: Int, timestamp: Long)
