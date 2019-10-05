package cryptocurrency.network.actors

import akka.actor.{Actor, Props}
import akka.util.Timeout
import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState}
import cryptocurrency.mining.Miner

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MineActor extends Actor {
  import MineActor._

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext = context.dispatcher

  def receive: Receive = active(BlockChainState(BlockChain()))

  def active(state: BlockChainState): Receive = {
    case MessageMine =>
      sender() ! MessageMine(updateState(state))
    case GetBlockChain => sender() ! state.blockChain
    case VerifyIntegrity =>
      sender() ! VerifyIntegrity(Miner.validateChain(state.blockChain))
  }

  def updateState(state: BlockChainState): String = {
    val nonce = Miner.proofOfWork(state.blockChain.hash, 3)
    val block = Block(state.blockChain.index + 1, state.blockChain.hash, nonce, System.currentTimeMillis(), previous = state.blockChain) :: state.blockChain

    context become active(BlockChainState(block))

    block.hash
  }
}

object MineActor {
  def props: Props = Props[MineActor]

  case class MessageMine(string: String)
  case class VerifyIntegrity(status: Boolean)

  case object VerifyIntegrity
  case object GetBlockChain
  case object MessageMine
}
