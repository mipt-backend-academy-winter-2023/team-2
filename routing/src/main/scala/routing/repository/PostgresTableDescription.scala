package routing.repository

import routing.model.{Node, Edge}
import zio.schema.{DeriveSchema, Schema}
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule {
  implicit val nodeSchema = DeriveSchema.gen[Node]
  implicit val edgeSchema = DeriveSchema.gen[Edge]

  val nodes = defineTable[Node]
  val edges = defineTable[Edge]

  val (id, category, name, location) = nodes.columns
  val (label, fromid, toid, distance) = edges.columns
}
