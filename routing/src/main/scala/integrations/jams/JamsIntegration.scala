package integrations.jams

import zio.{IO, ZIO}

trait JamsIntegration {
  def getJam(id: Int): IO[Serializable, JamValue]
}

object JamsIntegration {
  def getJam(id: Int): ZIO[JamsIntegration, Serializable, JamValue] =
    ZIO.serviceWithZIO[JamsIntegration](_.getJam(id))
}
