package cryptocurrency.network

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{BlockChain, BlockChainState}
import cryptocurrency.mining.Miner

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NetworkActor extends Actor {
  import NetworkActor._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  def receive: Receive = active(BlockChainState(BlockChain()))

  def active(state: BlockChainState): Receive = {
    case MineEvent =>
      val newBlock: BlockChain = Miner.generateNewBlock(state)

      // Overwrite the current state
      context become active(BlockChainState(newBlock))

      val logText = s"Mined block ${newBlock.index} with hash/nonce ${newBlock.hash}/${newBlock.nonce} (difficulty ${newBlock.difficulty})";

      // Log the mined block to the console
      log(logText)

      // Send the hash to the actor
      sender() ! MineEvent(logText)
    case RequestBlockChainEvent => sender() ! state.blockChain
    case VerifyIntegrityEvent =>
      sender() ! VerifyIntegrityEvent(Miner.validateChain(state.blockChain))
  }

  def log(text: String): Unit = {
    println(text)
  }
}

object NetworkActor {
  def props: Props = Props[NetworkActor]

  case class MineEvent(message: String)
  case class VerifyIntegrityEvent(status: Boolean)

  // Events
  case object VerifyIntegrityEvent
  case object RequestBlockChainEvent
  case object MineEvent
}
