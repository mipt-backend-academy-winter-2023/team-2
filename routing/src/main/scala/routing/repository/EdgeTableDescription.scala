package routing.repository

import routing.model.Edge
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait EdgeTableDescription extends PostgresJdbcModule {
  implicit val edgeSchema = DeriveSchema.gen[Edge]

  val edgesTable = defineTable[Edge]("edges")

  val (fId, fName, fFromId, ftoId) = edgesTable.columns
}
