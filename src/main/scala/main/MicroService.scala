package main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import db.{AccountDBIO, AccountTable, H2Component}
import spray.json._
import service.AccountService
import slick.jdbc.H2Profile.api._


case class TransferRequest(from: Long, to: Long, amount: BigDecimal)
case class TransferResponse(status: String)

trait TransferRequestJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val transferRequestFormat = jsonFormat3(TransferRequest)
  implicit val transferResponseFormat = jsonFormat1(TransferResponse)
}

class TransferRequestEndpoint(accountService: AccountService)
                             (implicit ec:ExecutionContext) extends TransferRequestJsonSupport {
  val routes = {
    pathPrefix("transfer") {
      (post & entity(as[TransferRequest])) { tr =>
        complete {
          accountService.transfer(tr.from, tr.to, tr.amount).map[ToResponseMarshallable] {
            case Right(_) => StatusCodes.OK -> TransferResponse("Ok")
            case Left(error) => StatusCodes.BadRequest -> TransferResponse(error)
          }
        }
      }
    }
  }
}

object MicroService extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  //db init - replace with production db (e.g. Postgres) in real service
  val database = Database.forConfig("accountDbH2")
  val dbio = new AccountDBIO with H2Component {
    val db = database
  }

  //since we are using H2 here, we load test data so that there's something
  //to demonstrate the service work
  Await.result(dbio.db.run(dbio.resetWith(ExampleData.data)), 1.seconds)

  val accountService = new AccountService(dbio)
  val transferRequestEndpoint = new TransferRequestEndpoint(accountService)

  Http().bindAndHandle(transferRequestEndpoint.routes,"0.0.0.0",8080)
}
