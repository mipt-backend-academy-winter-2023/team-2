package routing.jams

import routing.model.JamValue
import zio.{IO, ZIO}

trait JamService {
  def get(id: Int): IO[Serializable, JamValue]
}

object JamService {
  def get(id: Int): ZIO[JamService, Serializable, JamValue] =
    ZIO.serviceWithZIO[JamService](_.get(id))
}
