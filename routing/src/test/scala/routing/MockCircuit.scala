package routing

import circuitbreaker.MyCircuitBreaker
import nl.vroste.rezilience.CircuitBreaker.{WrappedError, CircuitBreakerCallError}
import zio.ZIO

final class MockCircuit extends MyCircuitBreaker {
  override def run[R, E, A](
      effect: ZIO[R, E, A]
  ): ZIO[R with MyCircuitBreaker, CircuitBreakerCallError[E], A] =
    effect.catchAll(error => ZIO.fail(new WrappedError(error)))
}
