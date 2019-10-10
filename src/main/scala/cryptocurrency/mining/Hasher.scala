package cryptocurrency.mining

import java.security.MessageDigest

object Hasher {
  val SHA256: MessageDigest = MessageDigest.getInstance("SHA-256")

  def hash(str: String): String = hash(str.getBytes("UTF-8")).map("%02x".format(_)).mkString
  def hash(bytes: Array[Byte]): Array[Byte] = SHA256.digest(bytes)
}
