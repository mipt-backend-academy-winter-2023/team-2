package images.utils

import zio.{Chunk}
import zio.stream.{ZChannel, ZPipeline}

object JpegValidation {

  final val JpegHeader = Chunk[Byte](0xff.toByte, 0xd8.toByte, 0xff.toByte)

  sealed trait Error extends Throwable
  object Error {
    case object Invalid extends Exception("This is not JPEG") with Error
    case object EmptyStream
        extends Exception("Input stream is empty")
        with Error
  }

  private def aux(
      buffer: Chunk[Byte]
  ): ZChannel[Any, Error, Chunk[Byte], Any, Error, Chunk[Byte], Any] = {
    ZChannel
      .readOrFail[Error, Chunk[Byte]](Error.EmptyStream)
      .flatMap { in =>
        val data = buffer ++ in
        if (data.length < JpegHeader.length) {
          aux(data)
        } else if (data.take(JpegHeader.length) == JpegHeader) {
          ZChannel.write(data) *> ZChannel.identity[Error, Chunk[Byte], Any]
        } else {
          ZChannel.fail(Error.Invalid)
        }
      }
  }

  final val pipeline = ZPipeline.fromChannel(aux(Chunk.empty[Byte]))
}
