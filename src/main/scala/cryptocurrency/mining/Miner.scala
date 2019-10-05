package cryptocurrency.mining

import cryptocurrency.blockchain.{Block, BlockChain, GenesisBlock}

import scala.annotation.tailrec

object Miner {

  // Does an attempt to get the proof of work (or nonce) of the previous hash.
  // The difficulty increases the hashing algorithms difficulty, which in turn will make
  // the mining process take significantly longer.
  def proofOfWork(lastHash: String, difficulty: Int): Long = {

    @tailrec
    def proofOfWorkHelper(lastHash: String, difficulty: Int,  proof: Long): Long = {
      if (validProof(lastHash, proof, difficulty)) proof
      else proofOfWorkHelper(lastHash, difficulty, proof + 1)
    }

    proofOfWorkHelper(lastHash, difficulty, 0)
  }

  // Validates if the given nonce belongs to the given hash.
  def validProof(lastHash: String, nonce: Long, difficulty: Int): Boolean = {
    val guessHash = Crypto.hash(lastHash ++ nonce.toString)
    (guessHash take difficulty) == ("0" * difficulty)
  }

  def validateChain(chain: BlockChain*): Boolean = {

    @tailrec
    def validateChainHelper(chain: BlockChain): Boolean = chain match {
      case b: Block =>
        val previous: BlockChain = b.previous
        if(!(previous.index + 1).equals(b.index)) false
        else validateChainHelper(previous)
      case GenesisBlock => true
      case _ => throw new RuntimeException()
    }

    validateChainHelper(chain.head)
  }

  def createHash(time: Long): String = Crypto.hash(time.toString)
  def createHash(data: String): String = Crypto.hash(data)
}
