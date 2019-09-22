package cryptocurrency.mining

import scala.annotation.tailrec

object Miner {

  def createHash(time: Long): String = Crypto.hash(time.toString)

  def proofOfWork(lastHash: String, difficulty: Int): Long = {
    @tailrec
    def powHelper(lastHash: String, difficulty: Int,  proof: Long): Long = {
      if (validProof(lastHash, proof, difficulty))
        proof
      else
        powHelper(lastHash, difficulty, proof + 1)
    }

    powHelper(lastHash, difficulty, 0)
  }

  def validProof(lastHash: String, proof: Long, difficulty: Int): Boolean = {
    val guessHash = Crypto.hash(lastHash ++ proof.toString)
    (guessHash take difficulty) == "0" * difficulty
  }
}
