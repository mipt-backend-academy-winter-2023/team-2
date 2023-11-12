package routing.jams

import routing.model.JamValue
import zio.{ZIO, IO}

trait Jams {
  def getJamValue(id: Int): IO[Serializable, JamValue]
}

object Jams {
  def getJamValue(id: Int): ZIO[Jams, Serializable, JamValue] =
    ZIO.serviceWithZIO[Jams](_.getJamValue(id))
}
