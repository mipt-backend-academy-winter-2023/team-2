package auth.jwt

import java.time.Clock
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

object JwtUtils {
  private val secretKey = "secret-key"
  def createToken(username: String): String = {
    val claim = JwtClaim(
      expiration = Some(3600),
      issuedAt = Some(Clock.systemUTC().instant().getEpochSecond),
      subject = Some(username)
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }
}

