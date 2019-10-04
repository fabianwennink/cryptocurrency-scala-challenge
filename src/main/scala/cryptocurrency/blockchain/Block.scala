package cryptocurrency.blockchain

import cryptocurrency.mining.Miner

// Serves as a single mined block in the full chain.
case class Block(index: Int, previousHash: String, nonce: Long, timestamp: Long = System.currentTimeMillis(), tail: BlockChain) extends BlockChain {
  val hash: String = Miner.createHash(index + previousHash + nonce + timestamp)
}

// Genesis block serves as the first block in the chain
object GenesisBlock extends Block(0, "0", 0, 0, null)
