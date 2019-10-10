package cryptocurrency.blockchain

import cryptocurrency.network.JsonProtocol
import spray.json._

import scala.annotation.tailrec

trait BlockChain {

  val index: Int
  val header: BlockHeader
  val transactions: List[Transaction]

  def ::(chain: BlockChain): BlockChain = chain match {
    case block:Block => Block(block.index, block.header, block.transactions, this)
    case _ => chain // If an invalid block is given, simply return it.
  }

  def toList: List[BlockChain] = {

    @tailrec
    def loop(chain: BlockChain, list: List[BlockChain]): List[BlockChain] = chain match {
      case b: Block => loop(b.previous, b :: list)
      case GenesisBlock => chain :: list
      case _ => list
    }

    loop(Seq(this).head, List.empty).reverse
  }
}

// The BlockChain is actually just a very large linked list. Each new Block has the previous Block linked to it.
object BlockChain {

  def apply[T](chain: BlockChain*): BlockChain = {
    if (chain.isEmpty)
      GenesisBlock // If the chain is empty, start the chain with the Genesis block
    else {
      val block = chain.head
      Block(block.index, block.header, block.transactions, apply(chain.tail: _*))
    }
  }
}

// Each Block in the sequence is immutable. The overall BlockChainState is mutable in the sense that the whole state
// will be replaced by the new state - which is a combination of the old BlockChain + the newest Block(Chain).
case class BlockChainState(blockChain: BlockChain, wallets: List[Wallet], pendingTransactions: List[Transaction])
