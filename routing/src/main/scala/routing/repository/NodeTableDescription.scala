package routing.repository

import routing.model.Node
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait NodeTableDescription extends PostgresJdbcModule {
  implicit val nodeSchema = DeriveSchema.gen[Node]

  val nodesTable = defineTable[Node]("nodes")

  val (fId, fNodeType, fName, fLatitude, fLongitude) = nodesTable.columns
}
