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

class Utils {}

object Utils {
  type TweetTuple = (Long, Timestamp, String, Long, String, Long, String, String, Boolean, String, Long, Int, Long, Boolean, String, String)

  val logger: Logger = LoggerFactory.getLogger(classOf[Utils])

  def getMutualsForUserId(userId: Long, restClient: TwitterRestClient)(implicit executor: ExecutionContext): ArrayBuffer[User] = {
    val mutuals = new ArrayBuffer[User]()

    @tailrec
    def mutualsHelper(cursor: Long = -1L): Unit = {
      if (cursor != 0) {
        val users = Await.result(restClient.friendsForUserId(user_id = userId, count = 200).map(_.data), Duration.Inf)
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
    restClient.userTimelineForUserId(user_id = userId, count = 30, exclude_replies = false).map(_.data)
  }

  def getDB = {
    val config = ConfigFactory.load("application")
    val url = config.getString("db.url")
    val username = config.getString("db.username")
    val password = config.getString("db.password")
    val driver = config.getString("db.driver")

    Database.forURL(url, username, password, null, driver)


    /*
    try {
      val action = DBIO.seq(
        users += (99986, "name 2", "screen name 2")
      )

      val future = db.run(action)
      Await.result(future, Duration.Inf)
    } finally db.close

     */
  }
}
