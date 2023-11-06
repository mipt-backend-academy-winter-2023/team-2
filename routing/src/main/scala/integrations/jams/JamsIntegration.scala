package integrations.jams

import zio.{IO, ZIO}

trait JamsIntegration {
  def getJam(id: Int): IO[Serializable, Int]
}

object JamsIntegration {
  def getJam(id: Int): ZIO[JamsIntegration, Serializable, Int] =
    ZIO.serviceWithZIO[JamsIntegration](_.getJam(id))
}
