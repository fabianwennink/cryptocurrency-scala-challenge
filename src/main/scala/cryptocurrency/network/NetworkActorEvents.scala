package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{BlockChain, BlockChainState, Transaction, Wallet}
import cryptocurrency.mining.{Broker, Miner}
import cryptocurrency.network.NetworkConfig.rootWallet

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class NetworkActorEvents extends Actor {
  import NetworkActorEvents._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  val state: BlockChainState = BlockChainState(BlockChain(), List(rootWallet))
  val pendingTransactions: List[Transaction] = List.empty

  def receive: Receive = active(state, pendingTransactions)

  def active(state: BlockChainState, pendingTransactions: List[Transaction]): Receive = {
    case MineEvent(address) =>
      val wallet = state.wallets.find(_.address == address)

      if (wallet.nonEmpty) {
        val newBlock: BlockChain = Miner.generateNewBlock(state, pendingTransactions, wallet.get)

        // Overwrite the current state
        context become active(BlockChainState(newBlock, state.wallets), List.empty)

        // Send the hash to the actor
        sender() ! MineSuccessEvent(s"Mined block ${newBlock.index} with hash/nonce ${newBlock.header.hash}/${newBlock.header.nonce} (difficulty ${newBlock.header.difficulty})")
      } else {
        sender() ! MineSuccessEvent(s"Unknown wallet address given to mine with.")
      }
    case RequestBlockChainEvent => sender() ! state.blockChain
    case VerifyIntegrityEvent =>
      sender() ! VerifyIntegrityEvent(Miner.validateChain(state.blockChain))
    case RegisterWalletEvent => {
      val newWallet = Wallet(NetworkConfig.walletNamePrefix + Random.nextInt(100000000).toString)
      val newWalletList = newWallet :: state.wallets

      // Overwrite the current state
      context become active(BlockChainState(state.blockChain, newWalletList), pendingTransactions)

      sender() ! RegisterWalletEvent(newWallet)
    }
    case GetWalletsEvent => {
      sender() ! GetWalletsEvent(state.wallets)
    }
    case GetWalletEvent(address) => {
      val wallet = state.wallets.find(_.address == address)
      if (wallet.nonEmpty) {
        val balance = Broker.getBalance(wallet.get.address, state.blockChain)
        sender() ! GetWalletSuccessEvent(balance)
      } else {
        sender() ! MineSuccessEvent(s"Unknown wallet address given to mine with.")
      }
    }
    case AddTransactionEvent(transaction) => {
      val correctedTransaction = Transaction(transaction.sender, transaction.receiver, transaction.amount, System.currentTimeMillis())

      if(Broker.validTransaction(correctedTransaction, state.blockChain, pendingTransactions)) {
        val pending = correctedTransaction :: pendingTransactions

        // Overwrite the current state
        context become active(BlockChainState(state.blockChain, state.wallets), pending)

        sender() ! AddTransactionSuccessEvent("The transaction has been stored.")
      }
    }
  }
}

object NetworkActorEvents {
  def props: Props = Props[NetworkActorEvents]

  case object RegisterWalletEvent
  case object VerifyIntegrityEvent
  case object RequestBlockChainEvent
  case object GetWalletsEvent

  case class GetWalletEvent(address: String)
  case class GetWalletsEvent(wallets: List[Wallet])
  case class MineEvent(address: String)
  case class VerifyIntegrityEvent(status: Boolean)
  case class RegisterWalletEvent(wallet: Wallet)
  case class AddTransactionEvent(transaction: Transaction)
  case class MineSuccessEvent(message: String)
  case class AddTransactionSuccessEvent(message: String)
  case class GetWalletSuccessEvent(balance: Int)
}
