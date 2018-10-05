package service

import db.{AccountDBIO, DBComponent, DBException}
import model.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AccountService(dbModule: AccountDBIO with DBComponent) {

  def transfer(fromAccountId: AccountId, toAccountId: AccountId, transferAmount: BigDecimal)
              (implicit ec: ExecutionContext) : Future[Either[String, Int]] = {
    if(transferAmount < 0) {
      return Future.successful(Left("Negative transfer amount"))
    }
    if(fromAccountId < 0) {
      Future.successful(Left("Negative transfer source ID"))
    }
    if(toAccountId < 0) {
      Future.successful(Left("Negative transfer destination ID"))
    }

    dbModule.db.run(dbModule.transfer(fromAccountId, toAccountId, transferAmount)).map {
      case Success(value) => Right(value)
      case Failure(DBException(msg)) => Left(msg)
      case Failure(_) => Left("Unknown error")
    }
  }

}
