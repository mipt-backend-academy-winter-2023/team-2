package routing

import routing.MockJamService.hashJamValue
import routing.jams.JamService
import routing.model.JamValue
import zio.{Task, ZIO}

final case class MockJamService(var queryBound: Int) extends JamService {

  final class NumberOfQueriesExceeded extends Exception
  override def get(id: Int): Task[JamValue] =
    if (queryBound > 0) {
      queryBound -= 1
      ZIO.succeed(hashJamValue(id))
    } else {
      ZIO.fail(new NumberOfQueriesExceeded)
    }
}

object MockJamService {
  def hashJamValue(id: Int): JamValue = JamValue(id)
}
