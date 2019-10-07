package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{BlockChain, BlockChainState, Transaction, Wallet}
import cryptocurrency.mining.Miner

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NetworkActor extends Actor {
  import NetworkActor._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  val state: BlockChainState = BlockChainState(BlockChain(), List.empty)
  var pendingTransactions: List[Transaction] = List.empty

  def receive: Receive = active(state, pendingTransactions)

  def active(state: BlockChainState, pendingTransactions: List[Transaction]): Receive = {
    case MineEvent =>
      val newBlock: BlockChain = Miner.generateNewBlock(state, pendingTransactions)

      // Overwrite the current state
      context become active(BlockChainState(newBlock, state.wallets), List.empty)

      // Send the hash to the actor
      sender() ! MineEvent(s"Mined block ${newBlock.index} with hash/nonce ${newBlock.header.hash}/${newBlock.header.nonce} (difficulty ${newBlock.header.difficulty})")
    case RequestBlockChainEvent => sender() ! state.blockChain
    case VerifyIntegrityEvent =>
      sender() ! VerifyIntegrityEvent(Miner.validateChain(state.blockChain))
    case RegisterWalletEvent => {
      val newWallet = Wallet(NetworkConfig.walletNamePrefix + (state.wallets.size + 1).toString)
      val newWalletList = newWallet :: state.wallets

      // Overwrite the current state
      context become active(BlockChainState(state.blockChain, newWalletList), pendingTransactions)

      sender() ! RegisterWalletEvent(newWallet)
    }
    case AddTransactionEvent(transaction) => {
      val correctedTransaction = Transaction(transaction.sender, transaction.receiver, transaction.amount, System.currentTimeMillis())
      val pending = correctedTransaction :: pendingTransactions

      // Overwrite the current state
      context become active(BlockChainState(state.blockChain, state.wallets), pending)

      sender() ! AddTransactionSuccessEvent("The transaction has been stored.")
    }
  }
}

object NetworkActor {
  def props: Props = Props[NetworkActor]

  case object RegisterWalletEvent
  case object VerifyIntegrityEvent
  case object RequestBlockChainEvent
  case object MineEvent

  case class MineEvent(message: String)
  case class VerifyIntegrityEvent(status: Boolean)
  case class RegisterWalletEvent(wallet: Wallet)
  case class AddTransactionEvent(transaction: Transaction)
  case class AddTransactionSuccessEvent(message: String)
}
