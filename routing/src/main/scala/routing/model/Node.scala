package routing.model

case class Node(
    id: Int,
    nodeType: Int,
    name: Option[String],
    latitude: Float,
    longitude: Float
)
