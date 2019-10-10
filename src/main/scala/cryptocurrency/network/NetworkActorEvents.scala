package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState, Transaction, Wallet}
import cryptocurrency.mining.{Broker, Miner}
import cryptocurrency.network.NetworkConfig.rootWallet

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class NetworkActorEvents extends Actor {
  import NetworkActorEvents._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  val state: BlockChainState = BlockChainState(BlockChain(), List[Wallet](rootWallet), List())

  def receive: Receive = active(state)

  def active(state: BlockChainState): Receive = {

    // Event: Will mine a new block and add it to the blockchain.
    case MineBlockEvent(address) => {
      val wallet = state.wallets.find(_.address == address)

      if (wallet.nonEmpty) {
        val newBlock: Block = Miner.generateNewBlock(state, wallet.get)

        // Overwrite the current state
        context become active(BlockChainState(newBlock :: state.blockChain, state.wallets, List()))

        // Send the hash to the actor
        sender() ! MineBlockSuccessEvent(s"Mined block ${newBlock.index} with hash/nonce ${newBlock.header.hash}/${newBlock.header.nonce} (difficulty ${newBlock.header.difficulty})")
      } else {
        sender() ! MineBlockSuccessEvent(s"Unknown wallet address given to mine with.")
      }
    }

    // Event: Returns the full blockchain.
    case RequestBlockChainEvent => {
      sender() ! RequestBlockChainEvent(state.blockChain)
    }

    // Event: Validates if the integrity of the blockchain is still good.
    case VerifyChainIntegrityEvent => {
      sender() ! VerifyChainIntegrityEvent(Miner.validateChain(state.blockChain))
    }

    // Event: Generates a new wallet.
    case GenerateNewWalletEvent => {
      val newWallet = Wallet(NetworkConfig.walletNamePrefix + Random.nextInt(100000000).toString)
      val newWalletList = newWallet :: state.wallets

      // Overwrite the current state
      context become active(BlockChainState(state.blockChain, newWalletList, state.pendingTransactions))

      sender() ! GenerateNewWalletEvent(newWallet)
    }

    // Event: Returns all registered wallets.
    case RequestsAllWalletsEvent => {
      sender() ! RequestsAllWalletsEvent(state.wallets)
    }

    // Event: Returns the balance of the given wallet.
    case RequestWalletBalanceEvent(address) => {
      val wallet = state.wallets.find(_.address == address)
      if (wallet.nonEmpty) {
        val balance = Broker.getBalance(wallet.get, state.blockChain)
        sender() ! RequestWalletBalanceSuccessEvent(balance)
      } else {
        sender() ! MineBlockSuccessEvent(s"Unknown wallet address given to mine with.")
      }
    }

    // Event: Adds the given transaction to the pending transactions pool.
    case RegisterTransactionEvent(transaction) => {
      val correctedTransaction = Transaction(transaction.sender, transaction.receiver, transaction.amount, System.currentTimeMillis())

      if(Broker.validTransaction(correctedTransaction, state.blockChain, state.pendingTransactions)) {
        val pending = correctedTransaction :: state.pendingTransactions

        // Overwrite the current state
        context become active(BlockChainState(state.blockChain, state.wallets, pending))

        sender() ! RegisterTransactionSuccessEvent("The transaction has been stored.")
      } else {
        sender() ! RegisterTransactionSuccessEvent("The transaction could not be stored.")
      }
    }
  }
}

object NetworkActorEvents {
  def props: Props = Props[NetworkActorEvents]

  case class MineBlockEvent(address: String)
  case class MineBlockSuccessEvent(message: String)

  case object RequestBlockChainEvent
  case class RequestBlockChainEvent(chain: BlockChain)

  case object VerifyChainIntegrityEvent
  case class VerifyChainIntegrityEvent(status: Boolean)

  case object GenerateNewWalletEvent
  case class GenerateNewWalletEvent(wallet: Wallet)

  case object RequestsAllWalletsEvent
  case class RequestsAllWalletsEvent(wallets: List[Wallet])

  case class RequestWalletBalanceEvent(address: String)
  case class RequestWalletBalanceSuccessEvent(balance: Int)

  case class RegisterTransactionEvent(transaction: Transaction)
  case class RegisterTransactionSuccessEvent(message: String)
}
