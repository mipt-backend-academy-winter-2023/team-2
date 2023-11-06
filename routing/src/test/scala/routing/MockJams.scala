package routing

import zio.{Task, ZIO}
import integrations.jams.{JamValue, JamsIntegration}

final class MockJams extends JamsIntegration {
  override def getJam(id: Int): Task[JamValue] =
    ZIO.succeed(JamValue(id))
}
