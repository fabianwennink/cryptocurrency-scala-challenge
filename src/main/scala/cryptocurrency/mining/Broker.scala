package cryptocurrency.mining

import cryptocurrency.blockchain._
import cryptocurrency.network.JsonProtocol._
import spray.json._

object Broker {

  // Returns the current balance of the given Wallet address.
  def getBalance(wallet: Wallet, chain: BlockChain): Int = {
    val transactions = getAllTransactions(chain)
    val headers = getAllBlockHeaders(chain)

    val received: Int = getReceivedBalance(wallet, transactions)
    val sent: Int = getSentBalance(wallet, transactions)
    val mining: Int = headers.filter(h => h.minedBy == wallet.address).map(h => h.reward).sum

    (mining - sent) + received
  }

  // Validates the transaction
  def validTransaction(transaction: Transaction, chain: BlockChain, pendingTransactions: List[Transaction]): Boolean = {
    val pendingReceived: Int = getReceivedBalance(transaction.sender, pendingTransactions)
    val pendingSent: Int = getSentBalance(transaction.sender, pendingTransactions)

    val pendingBalance = pendingReceived - pendingSent
    val balance = getBalance(transaction.sender, chain)

    (balance - pendingBalance) >= transaction.amount
  }

  private def getReceivedBalance(wallet: Wallet, transactions: List[Transaction]): Int = transactions.filter(txn => txn.receiver.address == wallet.address).map(h => h.amount).sum
  private def getSentBalance(wallet: Wallet, transactions: List[Transaction]): Int = transactions.filter(txn => txn.sender.address == wallet.address).map(h => h.amount).sum
  private def getAllBlockHeaders(bc: BlockChain): List[BlockHeader] = bc.toList.map(h => h.header)
  private def getAllTransactions(bc: BlockChain): List[Transaction] = bc.toList.filter(b => b.transactions.nonEmpty).flatMap(h => h.transactions)
}
