package services

import db.{AccountDBIO, DBComponent, DBException}
import domain.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AccountService(dbModule: AccountDBIO with DBComponent) {

  def transfer(from: AccountId, to: AccountId, transferAmount: BigDecimal)
              (implicit ec: ExecutionContext) : Future[Either[String, Int]] = {
    if(transferAmount < 0) {
      return Future.successful(Left("Negative transfer amount"))
    }
    if(from.value < 0) {
      Future.successful(Left("Negative transfer source ID"))
    }
    if(to.value < 0) {
      Future.successful(Left("Negative transfer destination ID"))
    }

    dbModule.db.run(dbModule.transfer(from, to, transferAmount)).map {
      case Success(value) => Right(value)
      case Failure(DBException(msg)) => Left(msg)
      case Failure(_) => Left("Unknown error")
    }
  }

}
