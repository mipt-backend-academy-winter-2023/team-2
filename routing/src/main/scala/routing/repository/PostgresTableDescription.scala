package routing.repository

import routing.model.{Node, Edge}
import zio.schema.{DeriveSchema, Schema}
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule {
  implicit val nodeSchema = DeriveSchema.gen[Node]
  implicit val edgeSchema = DeriveSchema.gen[Edge]

  val nodes = defineTable[Node]
  val edges = defineTable[Edge]

  val (id, is_intersection, name, lat, lon) = nodes.columns
  val (name, end1, end2) = edges.columns
}
