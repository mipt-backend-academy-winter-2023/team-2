package photo.api

import java.nio.file.{Files, Paths, Path}

import zio._
import zio.http._
//import zio.stream._
import zio.http.model.{Method, Status}
import zio.stream.{ZSink, ZStream, ZPipeline}

object HttpRoutes {
  val app: HttpApp[Any, Nothing] =
    Http.collectZIO[Request] {
/*      case req @ Method.POST -> !! / "add_photo" => {
        if (req.header(Header.ContentType).exists(_.mediaType == MediaType.multipart.`form-data`))
          for {
            _     <- ZIO.debug("Starting to read multipart/form stream")
            form  <- req.body.asStream
              .mapError(ex =>
                Response(
                  Status.InternalServerError,
                  body = Body.fromString(s"Failed to decode body as multipart/form-data (${ex.getMessage}"),
                ),
              )
            count <- form.fields.flatMap {
              case sb: FormField.StreamingBinary =>
                sb.data
              case _                             =>
                ZStream.empty
            }.run(ZSink.count)

            _ <- Response.text(s"Finished reading multipart/form stream, received $count bytes of data")
          } yield Response.text(count.toString)
        else ZIO.succeed(Response(status = Status.NotFound))
      }*/
/*      case req @ Method.POST -> !! / "add_photo" =>
        (for {
          bodyStream = req.body.asStream
          data = Body.fromStream(bodyStream)
          _ <- ZIO.logInfo(s"$data")
        } yield (data)).either.map {
          case Left(_)  => Response.status(Status.BadRequest)
          case Right(_) => Response(body = data)
        }*/
/*      case request @ Method.PUT -> !! / "upload" =>
        (for {
          path <- ZIO.attempt(Files.createTempFile("uploaded", null))
          _ <- request.body.asStream
            .via(ZPipeline.deflate())
            .run(ZSink.fromPath(path))
        } yield (path)).either.map {
          case Left(e)  => Response.text(e.toString)
          case Right(_) => Response(Status.Ok)
        }

      case request @ Method.GET -> !! / "download" =>
        ZStream.fromPath(Paths.get("uploaded")).runFold("")(_ + _).either.map {
          case Left(e)  => Response.text(e.toString)
          case Right(s) => Response.text(s)
        }*/
//          _ <- ZIO.attempt(Files.createNewFile(Paths.get("/tmp/uploaded")))
      case request @ Method.PUT -> !! / "upload" =>
        (for {
          nodeIdStr <- ZIO
            .fromOption(
              request.url.queryParams
                .get("nodeId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide nodeId argument"))
          _ <- ZIO.attempt(Files.createFile(Paths.get(s"/tmp/uploaded$nodeIdStr"))).either.map { case _ => null }
          path = Paths.get(s"/tmp/uploaded$nodeIdStr")
          _ <- request.body.asStream
            .via(ZPipeline.deflate())
            .run(ZSink.fromPath(path))
        } yield (path, nodeIdStr)).either.map {
          case Left(e)  => Response.text(e.toString)
          case Right(_) => Response(Status.Ok)
        }

      case request @ Method.GET -> !! / "download" =>
        (for {
          nodeIdStr <- ZIO
            .fromOption(
              request.url.queryParams
                .get("nodeId")
                .flatMap(_.headOption)
            )
            .tapError(_ => ZIO.logError("Provide nodeId argument"))
        } yield (nodeIdStr)).either.map {
          case Left(e)  => ZIO.succeed(Response.text(e.toString))
          case Right(nodeIdStr) =>
            ZStream.fromPath(Paths.get(s"/tmp/uploaded$nodeIdStr")).runFold("")(_ + " | " + _).either.map {
              case Left(e)  => Response.text(e.toString)
              case Right(s) => Response.text(s)
            }
        }.flatten

        /*val stream = req.body.asStream
        val data = Body.fromStream(stream)
        Response(body = data)*/
//      case _ => Response.text("Invalid route!")
  }/*.catchAllCauseZIO { cause =>
    ZIO.logErrorCause("ERROR", cause).as(Response.status(Status.InternalServerError))
  }*/
}

