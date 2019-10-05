package cryptocurrency.blockchain

import cryptocurrency.mining.Miner

// Serves as a single mined block in the full chain.
case class Block(index: Int, previousHash: String, nonce: Long, difficulty: Int, timestamp: Long, previous: BlockChain) extends BlockChain {
  val hash: String = Miner.createHash(previousHash ++ nonce.toString)
}

// Genesis block serves as the first block in the chain
case object GenesisBlock extends BlockChain {
  val index: Int = 0
  val hash: String = "1"
  val nonce: Long = 82
  val difficulty: Int = 1
  val timestamp: Long = System.currentTimeMillis()
}
