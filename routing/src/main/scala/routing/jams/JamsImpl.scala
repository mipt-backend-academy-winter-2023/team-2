package routing.jams

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.client3.{SttpBackend, basicRequest}
import sttp.client3.circe.asJson
import sttp.model.Uri
import routing.model.JamValue
import zio.{Task, ZIO, ZLayer}

class JamsImpl(
    httpClient: SttpBackend[Task, Any]
) extends Jams {

  def getJamValue(id: Int): Task[JamValue] = {
    implicit val jamValueDecoder: Decoder[JamValue] = deriveDecoder
    val request = basicRequest
      .get(Uri(s"http://jams:8080/jam/$id"))
      .response(asJson[JamValue])
    httpClient.send(request).flatMap(response => ZIO.fromEither(response.body))
  }
}

object JamsImpl {
  val live: ZLayer[SttpBackend[Task, Any], Nothing, Jams] =
    ZLayer.fromFunction(new JamsImpl(_))
}
