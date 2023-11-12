package routing

import zio.{Task, ZIO}
import jams.Jams
import routing.model.JamValue

final class MockJams extends Jams {
  override def getJamValue(id: Int): Task[JamValue] =
    ZIO.succeed(JamValue(id))
}
