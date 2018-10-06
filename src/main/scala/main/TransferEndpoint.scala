package main

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, pathPrefix, post}
import service.AccountService
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext

case class TransferRequest(from: Long, to: Long, amount: BigDecimal)
case class TransferResponse(status: String)

trait TransferJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val transferRequestFormat = jsonFormat3(TransferRequest)
  implicit val transferResponseFormat = jsonFormat1(TransferResponse)
}

class TransferEndpoint(accountService: AccountService)
                      (implicit ec:ExecutionContext) extends TransferJsonSupport {
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
