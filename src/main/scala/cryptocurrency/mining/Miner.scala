package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, BlockChainState, BlockHeader, GenesisBlock, Transaction, Wallet}

import scala.annotation.tailrec
import cryptocurrency.network.NetworkConfig.{blockReward, defaultMiningDifficulty, miningDifficultyIncreaseRate}
import spray.json._
import cryptocurrency.network.JsonProtocol._

object Miner {

  // Generates a new Block for the BlockChain. When mining for the solution, the header of the
  // previous block, the candidate nonce and the current timestamp (of the request, not of the attempt) is used
  // to form a hash.
  def generateNewBlock(state: BlockChainState, pendingTransactions: List[Transaction], miner: Wallet, reward: Int = blockReward, timestamp: Long = System.currentTimeMillis()): BlockChain = {
    val diff = calculateDifficulty(state.blockChain)
    val nonce = generateProofOfWork(state.blockChain.header, timestamp, diff)
    val hash = createHash(state.blockChain.header, nonce.toString ++ timestamp.toString)
    val merkleRoot = createMerkle(pendingTransactions)
    val header = generateNewBlockHeader(hash, nonce, miner, merkleRoot, diff, reward, timestamp)

    Block(state.blockChain.index + 1, header, pendingTransactions, state.blockChain) :: state.blockChain
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
    val candidate = createHash(previousBlock, nonce.toString ++ timestamp.toString)
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
        if(!(previous.index + 1).equals(b.index) || !validProofOfWork(previous.header, b.header.nonce, b.header.difficulty, b.header.timestamp)) false
        else validateChainHelper(previous)
      case GenesisBlock => true
      case _ => false // If an unknown object is found, return false by default
    }

    validateChainHelper(chain.head)
  }

//  def getHeaders(chain: BlockChain): List[BlockHeader] = {
//
//    @tailrec
//    def loop(chain: BlockChain, acc: List[BlockHeader]): List[BlockHeader] = chain match {
//      case b: Block => loop(b.previous, b.header :: acc)
//      case GenesisBlock | _ => acc
//    }
//
//    loop(chain, List.empty)
//  }

  // Calculates the difficulty based on the amount of blocks present in the chain.
  // With the current implementation, the difficulty increases with every X blocks mined.
  def calculateDifficulty(chain: BlockChain): Int = Math.floor(chain.index / miningDifficultyIncreaseRate).toInt + defaultMiningDifficulty

  def createHash(block: BlockHeader, data: String): String = Crypto.hash(block.toJson.toString ++ data)

  def createMerkle(transactions: List[Transaction]): String = {
    if(transactions.nonEmpty) Crypto.hash(transactions.map(_.toJson).toString)
    else ""
  }
}
