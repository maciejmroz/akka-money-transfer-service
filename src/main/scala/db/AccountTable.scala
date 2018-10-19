package db

import domain.{Account, AccountId}

trait AccountTable { self: DBComponent =>
  import profile.api._

  implicit lazy val accountIdColumnType = MappedColumnType.base[AccountId, Long](_.value, AccountId)

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {


    def id = column[AccountId]("id", O.PrimaryKey, O.AutoInc)
    def balance = column[BigDecimal]("balance")

    def * = (balance, id).mapTo[Account]

  }

  lazy val query = TableQuery[AccountTable]
}
