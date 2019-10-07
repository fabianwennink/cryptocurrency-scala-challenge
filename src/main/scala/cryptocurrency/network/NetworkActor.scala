package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{BlockChain, BlockChainState, Wallet}
import cryptocurrency.mining.Miner

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NetworkActor extends Actor {
  import NetworkActor._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  def receive: Receive = active(BlockChainState(BlockChain(), List.empty))

  def active(state: BlockChainState): Receive = {
    case MineEvent =>
      val newBlock: BlockChain = Miner.generateNewBlock(state)

      // Overwrite the current state
      context become active(BlockChainState(newBlock, state.wallets))

      val logText = s"Mined block ${newBlock.index} with hash/nonce ${newBlock.header.hash}/${newBlock.header.nonce} (difficulty ${newBlock.header.difficulty})";

      // Log the mined block to the console
      log(logText)

      // Send the hash to the actor
      sender() ! MineEvent(logText)
    case RequestBlockChainEvent => sender() ! state.blockChain
    case VerifyIntegrityEvent =>
      println(Miner.getHeaders(state.blockChain))
      sender() ! VerifyIntegrityEvent(Miner.validateChain(state.blockChain))
    case RegisterWalletEvent => {
      val newWallet = Wallet(NetworkConfig.walletNamePrefix + (state.wallets.size + 1).toString)
      val newWalletList = newWallet :: state.wallets

      context become active(BlockChainState(state.blockChain, newWalletList))

      sender() ! RegisterWalletEvent(newWallet)
    }
  }

  def log(text: String): Unit = {
    println(text)
  }
}

object NetworkActor {
  def props: Props = Props[NetworkActor]

  case class MineEvent(message: String)
  case class VerifyIntegrityEvent(status: Boolean)
  case class RegisterWalletEvent(wallet: Wallet)

  // Events
  case object RegisterWalletEvent
  case object VerifyIntegrityEvent
  case object RequestBlockChainEvent
  case object MineEvent
}
