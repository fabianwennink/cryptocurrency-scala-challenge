package cryptocurrency.blockchain

trait BlockChain {

  val index: Int
  val hash: String
  val nonce: Long
  val timestamp: Long

  def ::(chain: BlockChain): Either[Block, BlockChain] = chain match {
    case block:Block => Left(Block(block.index, hash, block.nonce, block.timestamp, this))
    case _ => Right(chain) // If an invalid block is given, simply return it.
  }
}

// The BlockChain is actually just a very large linked list. Each new Block has the previous Block linked to it.
object BlockChain {

  def apply[T](chain: BlockChain*): BlockChain = {

    // If the chain is empty, start the chain with the Genesis block
    if (chain.isEmpty)
      GenesisBlock
    else {
      val block = chain.head
      Block(block.index, block.hash, block.nonce, block.timestamp, apply(chain.tail: _*))
    }
  }
}

// Each Block in the sequence is immutable. The overall BlockChainState is mutable in the sense that the whole state
// will be replaced by the new state - which is a combination of the old BlockChain + the newest Block(Chain).
case class BlockChainState(blockChain: BlockChain)
