package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState, GenesisBlock}

import scala.annotation.tailrec

object Miner {

  val miningDifficultyIncreaseRate = 25

  def generateNewBlock(state: BlockChainState): BlockChain = {
    val diff = Miner.calculateDifficulty(state.blockChain)
    val nonce = Miner.generateProofOfWork(state.blockChain.hash, diff)

    Block(state.blockChain.index + 1, state.blockChain.hash, nonce, diff, System.currentTimeMillis(), previous = state.blockChain) :: state.blockChain
  }

  // Does an attempt to get the proof of work (or nonce) of the previous hash.
  // The difficulty increases the hashing algorithms difficulty, which in turn will make
  // the mining process take significantly longer.
  def generateProofOfWork(lastHash: String, difficulty: Int): Long = {

    @tailrec
    def proofOfWorkHelper(lastHash: String, difficulty: Int, nonce: Long): Long = {
      if (validProofOfWork(lastHash, nonce, difficulty)) nonce
      else proofOfWorkHelper(lastHash, difficulty, nonce + 1)
    }

    proofOfWorkHelper(lastHash, difficulty, 0)
  }

  // Validates if the given nonce belongs to the given hash.
  def validProofOfWork(lastHash: String, nonce: Long, difficulty: Int): Boolean = {
    val guessHash = Crypto.hash(lastHash ++ nonce.toString)
    (guessHash take difficulty) == ("0" * difficulty)
  }

  // Validates the integrity of the whole chain. If somehow the chain
  // is not correct, it means that the chain was tempered with in some way.
  def validateChain(chain: BlockChain*): Boolean = {

    @tailrec
    def validateChainHelper(chain: BlockChain): Boolean = chain match {
      case b: Block =>
        val previous: BlockChain = b.previous

        // Check if the index order is correct and if the hash and nonce are correct
        if(!(previous.index + 1).equals(b.index) || !validProofOfWork(previous.hash, b.nonce, b.difficulty)) false
        else validateChainHelper(previous)
      case GenesisBlock => true
      case _ => false // If an unknown object is found, return false by default
    }

    validateChainHelper(chain.head)
  }

  // Calculates the difficulty based on the amount of blocks present in the chain.
  // With the current implementation, the difficulty increases with every X blocks mined.
  def calculateDifficulty(chain: BlockChain): Int = Math.floor(chain.index / miningDifficultyIncreaseRate).toInt + 1

  def createHash(time: Long): String = Crypto.hash(time.toString)
  def createHash(data: String): String = Crypto.hash(data)
}
