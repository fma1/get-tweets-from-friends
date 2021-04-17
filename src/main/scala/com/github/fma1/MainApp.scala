package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet, User}
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object MainApp extends App {
  val consumerToken: ConsumerToken = _
  val accessToken: AccessToken = _

  val restClient: TwitterRestClient = TwitterRestClient(consumerToken, accessToken)

  val userId = 1260597074036355072L

  val db = Utils.db

  class UsersTable(tag: Tag) extends Table[(Int, String, String)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    def screenName = column[String]("screen_name")

    def * = (id, name, screenName)
  }
  val usersTable = TableQuery[UsersTable]

  class TweetsTable(tag: Tag) extends Table[(Int, Timestamp, String, Int, String, Int, String, String, Boolean, Int, String, Int, Int, Long, Boolean, String, String)](tag, "users") {
    // TODO: Add all columns for Tweets table
    // TODO: FIXME
    def id = column[Int]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    def screenName = column[String]("screen_name")

    def * = (id, name, screenName)
  }
  val tweetsTable = TableQuery[TweetsTable]

  val users = Utils.getMutualsForUserId(userId, restClient)
  val arrayBufOfFutures: ArrayBuffer[Future[Seq[Tweet]]] = users.map(mutual =>
    Utils.getRecentTweetsForUserId(mutual.id, restClient))
  val futureOfArrayBuf: Future[ArrayBuffer[Seq[Tweet]]] = Future.sequence(arrayBufOfFutures)

  val dbFutures = ArrayBuffer[Future[Unit]]()

  try {
    // TODO: Add users first, then add tweets
    futureOfArrayBuf
      .foreach(arrayBuf =>
        arrayBuf.foreach(seqTweet =>
          seqTweet.foreach(tweet => {
            val action = DBIO.seq(
              // TODO: Add method to convert Tweets and Users into tuples
              // users += (99986, "name 2", "screen name 2")
            )
            dbFutures.addOne(db.run(action))
          })))
  } finally db.close


  Thread.sleep(200000)
}
