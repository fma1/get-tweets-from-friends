package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object MainApp extends App {
  System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info")
  /*
  val consumerToken: ConsumerToken = _
  val accessToken: AccessToken = _

  val restClient: TwitterRestClient = TwitterRestClient(consumerToken, accessToken)

  val userId = 1260597074036355072L

  Utils.printRecentTweetsForUserId(userId, restClient)

  Thread.sleep(200000)
   */


  val config = ConfigFactory.load("application")
  val url = config.getString("db.url")
  val username = config.getString("db.username")
  val password = config.getString("db.password")
  val driver = config.getString("db.driver")

  val db = Database.forURL(url, username, password, null, driver)

  class Users(tag: Tag) extends Table[(Int, String, String)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    def screenName = column[String]("screen_name")

    def * = (id, name, screenName)
  }
  val users = TableQuery[Users]

  try {
    val action = DBIO.seq(
      users += (99986, "name 2", "screen name 2")
    )

    val future = db.run(action)
    Await.result(future, Duration.Inf)
  } finally db.close
}
