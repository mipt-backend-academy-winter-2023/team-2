package integrations.jams

import sttp.client3.circe.asJson
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri
import zio.{Task, ZIO, ZLayer}

class JamsIntegrationImpl(
    httpClient: SttpBackend[Task, Any]
) extends JamsIntegration {
  val baseUri: Uri = uri"http://jams:8080/jam/"

  def getJam(id: Int): Task[Int] = {
    val url = baseUri.addPath(s"$id")

    val request =
      basicRequest
        .get(url)
        .response(asJson[Int])

    httpClient.send(request).flatMap(response => ZIO.fromEither(response.body))
  }
}

object JamsIntegrationImpl {
  val live: ZLayer[SttpBackend[Task, Any], Nothing, JamsIntegration] =
    ZLayer.fromFunction(new JamsIntegrationImpl(_))
}
