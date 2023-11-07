package routing.jams

import routing.model.JamValue
import zio.{IO, Task, ZIO, ZLayer}
import sttp.client3._
import sttp.client3.circe._
import sttp.model.Uri
class JamServiceImpl(client: SttpBackend[Task, Any]) extends JamService {
  override def get(id: Int): Task[JamValue] = {
    val uri = Uri(s"http://jams:8080/jam/$id")
    val request = basicRequest.get(uri).response(asJson[JamValue])

    for {
      response <- client.send(request)
      body = response.body
      value <- ZIO.fromEither(body)
    } yield value
  }
}

object JamServiceImpl {
  val live: ZLayer[SttpBackend[Task, Any], Nothing, JamService] =
    ZLayer.fromFunction(new JamServiceImpl(_))
}
