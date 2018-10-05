package db

import slick.jdbc.JdbcProfile

trait DBComponent {
  val profile: JdbcProfile

  import profile.api._

  val db: Database
}

trait H2Component extends DBComponent {
  val profile = slick.jdbc.H2Profile
}

case class DBException(message: String) extends Exception