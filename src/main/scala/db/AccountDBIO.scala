package db

import model.AccountId

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

//TODO: Get rid of inheritance here.
//This design does not extend into DBIOs involving multiple tables (joins in SQL)
//The problem is that everything depends on the same JdbcProfile ... need to think about it :)
class AccountDBIO extends AccountTable { self: DBComponent =>

  import profile.api._

  def deposit(accountId: AccountId, depositAmount: BigDecimal)
             (implicit ec: ExecutionContext) : DBIO[Try[Int]] = {
    val target = query
      .filter(_.id === accountId)
      .map(_.balance)
    target.result.headOption.flatMap {
      case Some(currentBalance) => target.update(currentBalance + depositAmount)
      case None => DBIO.failed(DBException("Deposit failed"))
    }.asTry
  }

  //withdraw makes sure that balance cannot become negative
  def withdraw(accountId: AccountId, withdrawAmount: BigDecimal)
              (implicit ec: ExecutionContext) : DBIO[Try[Int]] = {
    val target = query
      .filter(r => (r.id === accountId) && (r.balance >= withdrawAmount))
      .map(_.balance)
    target.result.headOption.flatMap {
      case Some(currentBalance) => target.update(currentBalance - withdrawAmount)
      case None => DBIO.failed(DBException("Withdraw failed"))
    }.asTry
  }

  //transfer is simply a sequence of withdraw and then deposit, running inside single transaction
  def transfer(fromAccountId: AccountId, toAccountId: AccountId, transferAmount: BigDecimal)
              (implicit ec: ExecutionContext) : DBIO[Try[Int]] = {
    withdraw(fromAccountId, transferAmount).flatMap {
      case Success(_) => deposit(toAccountId, transferAmount)
      case Failure(e) => DBIO.failed(e)
    }.transactionally
  }
}
