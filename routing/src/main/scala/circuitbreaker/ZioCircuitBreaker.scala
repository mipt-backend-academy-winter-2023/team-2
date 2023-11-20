package circuitbreaker

import nl.vroste.rezilience.CircuitBreaker.CircuitBreakerCallError
import zio.ZIO

trait ZioCircuitBreaker {
  def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZioCircuitBreaker, CircuitBreakerCallError[E], A]
}

object ZioCircuitBreaker {
  def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZioCircuitBreaker, CircuitBreakerCallError[E], A] =
    ZIO.serviceWithZIO[ZioCircuitBreaker](_.run(effect))
}
