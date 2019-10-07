package cryptocurrency.blockchain

trait BlockChain {

  val index: Int
  val header: BlockHeader
  val transactions: List[Transaction]

  def ::(chain: BlockChain): BlockChain = chain match {
    case block:Block => Block(block.index, block.header, block.transactions ,this)
    case _ => chain // If an invalid block is given, simply return it.
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
case class BlockChainState(blockChain: BlockChain, wallets: List[Wallet])
