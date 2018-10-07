import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import slick.jdbc.H2Profile.api._
import db.{AccountDBIO, H2Component}
import main.{ExampleData, TransferEndpoint, TransferRequest, TransferResponse}
import service.AccountService

class TransferEndpointSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  //TODO: It's a copy of setup in MicroService object so it's tempting to refactor and remove code duplication
  val database = Database.forConfig("accountDbH2")
  val dbio = new AccountDBIO with H2Component {
    val db = database
  }

  Await.result(dbio.db.run(dbio.resetWith(ExampleData.data)), 1.seconds)

  val accountService = new AccountService(dbio)
  val transferRequestEndpoint = new TransferEndpoint(accountService)

  //imports routes and JSON serializers, but prevents testing multiple endpoints in single spec
  //(which probably shouldn't be done anyway)
  import transferRequestEndpoint._

  //tests

  "/transfer endpoint" should "respond with OK to valid request" in {
    Post(s"/transfer", TransferRequest(1,2,10.5)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Ok")
    }
  }

  it should "respond with BadRequest when transfer source is invalid" in {
    Post(s"/transfer", TransferRequest(-1,2,10)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Withdraw failed")
    }

    Post(s"/transfer", TransferRequest(25,2,10)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Withdraw failed")
    }
  }

  it should "respond with BadRequest when transfer destination is invalid" in {
    Post(s"/transfer", TransferRequest(1,-2,10)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Deposit failed")
    }

    Post(s"/transfer", TransferRequest(1,25,10)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Deposit failed")
    }
  }

  it should "respond with BadRequest when trying to transfer negative amount" in {
    Post(s"/transfer", TransferRequest(1,2,-33.2)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Negative transfer amount")
    }
  }

  it should "respond with BadRequest when trying to transfer more than available" in {
    Post(s"/transfer", TransferRequest(1,2,150.34)) ~> routes ~> check {
      status shouldBe BadRequest
      contentType shouldBe `application/json`
      responseAs[TransferResponse] shouldBe TransferResponse("Withdraw failed")
    }
  }
}