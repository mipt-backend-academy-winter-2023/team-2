package data.api

import io.circe.jawn.decode
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import zio.ZIO
import zio.http._
import zio.http.model.Status.{Ok, BadRequest, Forbidden}
import zio.http.model.{Method, Status}

object HttpRoutes {
  val app: HttpApp[Any, Response] =
    Http.collectZIO[Request] {

    }
}

