package cryptocurrency.blockchain

// Serves as a single mined block in the full chain.
case class BlockHeader(hash: String, nonce: Long, difficulty: Int, reward: Int, timestamp: Long)
case class Block(index: Int, header: BlockHeader, previous: BlockChain) extends BlockChain

// Genesis block serves as the first block in the chain
case object GenesisBlock extends BlockChain {
  val index: Int = 0
  val header: BlockHeader = BlockHeader(
    "0000000000000000000000000000000000000000000000000000000000000000",
    0, 0, 0, System.currentTimeMillis()
  )
}
