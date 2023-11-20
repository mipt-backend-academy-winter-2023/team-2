package routing

import circuitbreaker.ZioCircuitBreaker
import nl.vroste.rezilience.CircuitBreaker.{WrappedError, CircuitBreakerCallError}
import zio.ZIO

final class MockCircuit extends ZioCircuitBreaker {
  override def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with ZioCircuitBreaker, CircuitBreakerCallError[E], A] =
    effect.catchAll(error => ZIO.fail(new WrappedError(error)))
}
