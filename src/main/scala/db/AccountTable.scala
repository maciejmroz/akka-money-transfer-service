package db

import model.{Account, AccountId}

trait AccountTable { self: DBComponent =>
  import profile.api._

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {


    def id = column[AccountId]("id", O.PrimaryKey, O.AutoInc)
    def balance = column[BigDecimal]("balance")

    def * = (balance, id).mapTo[Account]

  }

  lazy val query = TableQuery[AccountTable]
}
