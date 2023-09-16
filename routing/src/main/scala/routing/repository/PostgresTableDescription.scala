package routing.repository

import routing.model.Node
import zio.schema.{DeriveSchema, Schema}
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule {
  implicit val nodeSchema = DeriveSchema.gen[Node]

  val nodes = defineTable[Node]

  val (category, name, location) = nodes.columns
}
