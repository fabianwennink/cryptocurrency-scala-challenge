package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState, BlockHeader, GenesisBlock, Transaction, Wallet}

import scala.annotation.tailrec
import cryptocurrency.network.NetworkConfig.{blockReward, defaultMiningDifficulty, miningDifficultyIncreaseRate}
import spray.json._
import cryptocurrency.network.JsonProtocol._

object Miner {

  // Generates a new Block for the BlockChain. When mining for the solution, the header of the
  // previous block, the difficulty, the candidate nonce and the current timestamp is used to form a hash.
  // Any pending transaction will be stored in the new block.
  def generateNewBlock(state: BlockChainState, miner: Wallet, reward: Int = blockReward): Block = {
    val timestamp = System.currentTimeMillis()
    val difficulty = calculateDifficulty(state.blockChain)
    val nonce = generateProofOfWork(state.blockChain.header, timestamp, difficulty)
    val hash = createHash(state.blockChain.header, difficulty.toString ++ nonce.toString ++ timestamp.toString)
    val merkleRoot = createMerkle(state.pendingTransactions)
    val header = generateNewBlockHeader(hash, nonce, miner, merkleRoot, difficulty, reward, timestamp)

    Block(state.blockChain.index + 1, header, state.pendingTransactions, state.blockChain)
  }

  private def generateNewBlockHeader(hash: String, nonce: Long, minedBy: Wallet, merkleRoot: String, difficulty: Int, reward: Int, timestamp: Long): BlockHeader = {
    BlockHeader(hash, nonce, minedBy.address, merkleRoot, difficulty, reward, timestamp)
  }

  // Does an attempt to get the proof of work (or nonce) of the previous hash.
  // The difficulty increases the hashing algorithms difficulty, which in turn will make
  // the mining process take significantly longer.
  def generateProofOfWork(previousBlock: BlockHeader, timestamp: Long, difficulty: Int): Long = {

    @tailrec
    def proofOfWorkHelper(previousBlock: BlockHeader, difficulty: Int, nonce: Long, timestamp: Long): Long = {
      if (validProofOfWork(previousBlock, nonce, difficulty, timestamp)) nonce
      else proofOfWorkHelper(previousBlock, difficulty, nonce + 1, timestamp)
    }

    proofOfWorkHelper(previousBlock, difficulty, 0, timestamp)
  }

  // Validates if the given nonce belongs to the given hash.
  def validProofOfWork(previousBlock: BlockHeader, nonce: Long, difficulty: Int, timestamp: Long): Boolean = {
    val candidate = createHash(previousBlock, difficulty.toString ++ nonce.toString ++ timestamp.toString)
    (candidate take difficulty) == ("0" * difficulty)
  }

  // Validates the integrity of the whole chain. If somehow the chain
  // is not correct, it means that the chain was tempered with in some way.
  def validateChain(chain: BlockChain*): Boolean = {

    @tailrec
    def loop(chain: BlockChain): Boolean = chain match {
      case b: Block =>
        val previous: BlockChain = b.previous

        // Check if the index order is correct and if the hash and nonce are correct
        if(!(previous.index + 1).equals(b.index)
          || !validProofOfWork(previous.header, b.header.nonce, b.header.difficulty, b.header.timestamp)
          || createMerkle(b.transactions) != b.header.merkleRoot) false
        else loop(previous)
      case GenesisBlock => true
      case _ => false // If an unknown object is found, return false by default
    }

    loop(chain.head)
  }

  // Calculates the difficulty based on the amount of blocks present in the chain.
  // With the current implementation, the difficulty increases with every X blocks mined.
  private def calculateDifficulty(chain: BlockChain): Int = Math.floor(chain.index / miningDifficultyIncreaseRate).toInt + defaultMiningDifficulty

  // Hashes the given BlockHeader and remaining Block data to form a SHA-256 hash.
  private def createHash(block: BlockHeader, data: String): String = Hasher.hash(block.toJson.toString ++ data)

  // Hashes the given list of Transactions to form a SHA-256 'merkle root' hash.
  // If the list of Transactions is empty, an empty string will be returned.
  private def createMerkle(transactions: List[Transaction]): String = {
    if(transactions.nonEmpty) Hasher.hash(transactions.toJson.toString) else ""
  }
}
