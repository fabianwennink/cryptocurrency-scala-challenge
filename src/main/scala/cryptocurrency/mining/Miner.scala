package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState, BlockHeader, GenesisBlock}

import scala.annotation.tailrec
import cryptocurrency.network.NetworkConfig.{blockReward, defaultMiningDifficulty, miningDifficultyIncreaseRate}
import spray.json._
import cryptocurrency.network.JsonProtocol._

object Miner {

  def generateNewBlock(state: BlockChainState, reward: Int = blockReward, timestamp: Long = System.currentTimeMillis()): BlockChain = {
    val diff = calculateDifficulty(state.blockChain)
    val nonce = generateProofOfWork(state.blockChain.header.hash, timestamp, diff)
    val hash = hashBlock(header)

    val header = generateNewBlockHeader(state.blockChain.header.hash, nonce, diff, reward, timestamp)

    Block(state.blockChain.index + 1, hash, nonce, diff, reward, timestamp, state.blockChain) :: state.blockChain
  }

  private def generateNewBlockHeader(hash: String, nonce: Long, difficulty: Int, reward: Int, timestamp: Long): BlockHeader = {
    BlockHeader(hash, nonce, difficulty, reward, timestamp)
  }

  // Does an attempt to get the proof of work (or nonce) of the previous hash.
  // The difficulty increases the hashing algorithms difficulty, which in turn will make
  // the mining process take significantly longer.
  def generateProofOfWork(lastHash: String, timestamp: Long, difficulty: Int): Long = {

    @tailrec
    def proofOfWorkHelper(lastHash: String, difficulty: Int, nonce: Long, timestamp: Long): Long = {
      if (validProofOfWork(lastHash, nonce, difficulty, timestamp)) nonce
      else proofOfWorkHelper(lastHash, difficulty, nonce + 1, timestamp)
    }

    proofOfWorkHelper(lastHash, difficulty, 0, timestamp)
  }

  // Validates if the given nonce belongs to the given hash.
  def validProofOfWork(lastHash: String, nonce: Long, difficulty: Int, timestamp: Long): Boolean = {
    val candidate = createHash(lastHash ++ nonce.toString ++ timestamp.toString)
    (candidate take difficulty) == ("0" * difficulty)
  }

  // Validates the integrity of the whole chain. If somehow the chain
  // is not correct, it means that the chain was tempered with in some way.
  def validateChain(chain: BlockChain*): Boolean = {

    @tailrec
    def validateChainHelper(chain: BlockChain): Boolean = chain match {
      case b: Block =>
        val previous: BlockChain = b.previous

        // Check if the index order is correct and if the hash and nonce are correct
        if(!(previous.index + 1).equals(b.index) || !validProofOfWork(previous.hash, b.nonce, b.difficulty, b.timestamp)) false
        else validateChainHelper(previous)
      case GenesisBlock => true
      case _ => false // If an unknown object is found, return false by default
    }

    validateChainHelper(chain.head)
  }

  // Calculates the difficulty based on the amount of blocks present in the chain.
  // With the current implementation, the difficulty increases with every X blocks mined.
  def calculateDifficulty(chain: BlockChain): Int = {
    Math.floor(chain.index / miningDifficultyIncreaseRate).toInt + defaultMiningDifficulty
  }

  def hashBlock(blockHeader: BlockHeader): String = {
    blockHeader.toJson.toString
  }

  def createHash(data: String): String = Crypto.hash(data)
}
