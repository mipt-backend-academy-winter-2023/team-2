package auth.security

object PasswordEncryptor {
  def encrypt(password: String): String = {
    password.reverse
  }
}
