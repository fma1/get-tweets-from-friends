package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{RatedData, Tweet, User, UserIds, Users}
import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile.api._
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Timestamp
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.implicitConversions

class Utils {}

object Utils {
  type UserTuple = (Long, String, String)
  type TweetTuple = (Long, Timestamp, String, Option[Long], Option[String], Option[Long], Option[String], Option[String], Boolean, Option[Long], Option[String], Option[Long], Int, Long, Boolean, String, String)

  val logger: Logger = LoggerFactory.getLogger(classOf[Utils])

  def getMutualsForUserId(userId: Long, restClient: TwitterRestClient)(implicit executor: ExecutionContext): ArrayBuffer[User] = {
    logger.info("Hello")
    System.err.println("getMutualsForUserId")
    val mutuals = new ArrayBuffer[User]()

    @tailrec
    def mutualsHelper(cursor: Long = -1L): Unit = {
      System.err.println("cursor: " + cursor)
      if (cursor != 0) {
        val users = Await.result(restClient.friendsForUserId(user_id = userId, count = 5).map(_.data), Duration.Inf)
        users match {
          case Users(users, next_cursor, _) =>
            mutuals.addAll(users.filter(_.following))
            mutualsHelper(next_cursor)
        }
      }
    }

    mutualsHelper()
    mutuals
  }

  def getRecentTweetsForUserId(userId: Long, restClient: TwitterRestClient)(implicit executor: ExecutionContext): Future[Seq[Tweet]] = {
    System.err.println("getRecentTweetsForUserId: " + userId)
    restClient.userTimelineForUserId(user_id = userId, count = 10, exclude_replies = false).map(_.data)
  }

  def getDB = {
    val config = ConfigFactory.load("application")
    val url = config.getString("db.url")
    val username = config.getString("db.username")
    val password = config.getString("db.password")
    val driver = config.getString("db.driver")

    Database.forURL(url, username, password, null, driver)
  }

  implicit class UserImprovement(val user: User) {
    def toUserTuple: UserTuple = {
      (user.id, user.name, user.screen_name)
    }
  }

  implicit class TweetImprovement(val tweet: Tweet) {
    def toTweetTuple: TweetTuple = {
      tweet match {
        case Tweet(
        _, _,
        created_at,
        _, _, _, _,
        favorite_count,
        _, _, _,
        id,
        id_str,
        in_reply_to_screen_name,
        in_reply_to_status_id,
        in_reply_to_status_id_str,
        in_reply_to_user_id,
        in_reply_to_user_id_str,
        is_quote_status,
        _, _, _,
        quoted_status_id,
        quoted_status_id_str,
        _, _,
        retweet_count,
        retweeted,
        _,
        source,
        text,
        _, _,
        user,
        _, _, _, _
        ) =>
          (
            id,
            Timestamp.from(created_at),
            id_str,
            in_reply_to_status_id,
            in_reply_to_status_id_str,
            in_reply_to_user_id,
            in_reply_to_user_id_str,
            in_reply_to_screen_name,
            is_quote_status,
            quoted_status_id,
            quoted_status_id_str,
            user.map(_.id),
            favorite_count,
            retweet_count,
            retweeted,
            source,
            text
          )
      }
    }
  }
}
