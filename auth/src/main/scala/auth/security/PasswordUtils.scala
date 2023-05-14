package auth.security

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import scala.collection.mutable;

object PasswordUtils {

  def getSHA(input: String): Array[Byte] = {
    val md = MessageDigest.getInstance("SHA-512")
    md.digest(input.getBytes(StandardCharsets.UTF_8))
  }

  def toHexString(hash: Array[Byte]): String = {
    val number: BigInteger = new BigInteger(1, hash)
    val hexString: mutable.StringBuilder =
      new mutable.StringBuilder(number.toString(16))
    while ({
      hexString.length < 32
    }) hexString.insert(0, '0')
    hexString.toString
  }

  def encode(password: String): String = {
    toHexString(getSHA(password))
  }
}
