package auth.repository

import auth.model.User
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule{
  implicit val customerSchema = DeriveSchema.gen[User]

  val userTable = defineTable[User]

  val (userId, username, password) = userTable.columns
}
