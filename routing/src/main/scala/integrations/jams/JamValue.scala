package integrations.jams

import io.circe.generic.JsonCodec

@JsonCodec
case class JamValue(jam_value: Int)