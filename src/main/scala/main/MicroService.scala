package main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._

import db.{AccountDBIO, H2Component}
import service.AccountService
import slick.jdbc.H2Profile.api._

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
  val transferRequestEndpoint = new TransferEndpoint(accountService)

  Http().bindAndHandle(transferRequestEndpoint.routes,"0.0.0.0",8080)
}
