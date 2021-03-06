package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.github.fma1.Utils._
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object MainApp extends App {
  val consumerToken: ConsumerToken = _
  val accessToken: AccessToken = _

  val restClient: TwitterRestClient = TwitterRestClient(consumerToken, accessToken)

  val userId = 1260597074036355072L

  val db = getDB

  class UsersTable(tag: Tag) extends Table[(Long, String, String)](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey) // This is the primary key column
    def name = column[String]("name")
    def screen_name = column[String]("screen_name")

    def * = (id, name, screen_name)
  }
  val usersTable = TableQuery[UsersTable]

  class TweetsTable(tag: Tag) extends Table[TweetTuple](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey) // This is the primary key column
    def created_at = column[Timestamp]("created_at")
    def id_str = column[String]("id_str")
    def in_reply_to_status_id = column[Long]("in_reply_to_status_id")
    def in_reply_to_status_id_str = column[String]("in_reply_to_status_id_str")
    def in_reply_to_user_id = column[Long]("in_reply_to_user_id")
    def in_reply_to_user_id_str = column[String]("in_reply_to_user_id_str")
    def in_reply_to_screen_name = column[String]("in_reply_to_screen_name")
    def is_quote_status = column[Boolean]("is_quote_status")
    def quoted_status_id = column[Long]("quoted_status_id")
    def quoted_status_id_str = column[String]("quoted_status_id_str")
    def user_id = column[Long]("user_id")
    def user = foreignKey("tweets_users_fk", user_id, usersTable)(_.id)
    def favorite_count = column[Int]("favorite_count")
    def retweet_count = column[Long]("retweet_count")
    def retweeted = column[Boolean]("retweed")
    def source = column[String]("source")
    def text = column[String]("text")

    def * = (id, created_at, id_str, in_reply_to_status_id.?, in_reply_to_status_id_str.?, in_reply_to_user_id.?, in_reply_to_user_id_str.?, in_reply_to_screen_name.?, is_quote_status, quoted_status_id.?, quoted_status_id_str.?, user_id.?, favorite_count, retweet_count, retweeted, source, text)
  }
  val tweetsTable = TableQuery[TweetsTable]

  val users = getMutualsForUserId(userId, restClient)
  val arrayBufOfFutures: ArrayBuffer[Future[Seq[Tweet]]] =
    users.map(mutual => getRecentTweetsForUserId(mutual.id, restClient))
  val futureOfArrayBuf: Future[ArrayBuffer[Seq[Tweet]]] =
    Future.sequence(arrayBufOfFutures)

  val dbFutures = ArrayBuffer[Future[Unit]]()

  try {
    futureOfArrayBuf onComplete {
      case Success(tweets) =>
        logger.info("Success.")
        usersTable ++= users.map(_.toUserTuple)
        tweetsTable ++= tweets.flatten.map(_.toTweetTuple)
      case Failure(exception) =>
        logger.error("Error getting tweets", exception)
    }
  } finally db.close


  Thread.sleep(200000)
}
