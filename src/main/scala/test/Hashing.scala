package test

import java.security.{MessageDigest, NoSuchAlgorithmException}

object Hashing {

  def sha256Hash(text: String): String = MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8")).map("%02x".format(_)).mkString("")
}
