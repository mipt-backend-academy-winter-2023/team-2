package auth.repo

import auth.model.User
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule{
  implicit val customerSchema = DeriveSchema.gen[User]

  val tableUsers = defineTable[User]

  val (username, password) = tableUsers.columns
}
