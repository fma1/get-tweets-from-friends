package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{RatedData, UserIds}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Utils {
  def printFriendsForUser(username: String, restClient: TwitterRestClient)(implicit executor: ExecutionContext): Unit = {
    restClient.friendIdsForUser(username) onComplete {
      case Success(value) =>
        value match {
          case RatedData(rate_limit, data) =>
            val userIds = data
            println(s"Rate limit: $rate_limit")
            userIds match {
              case UserIds(ids, next_cursor, previous_cursor) =>
                println(s"next_cursor: $next_cursor")
                println(s"previous_cursor: $previous_cursor")
                println("ids: ")
                println(ids.mkString(","))
            }
        }
      case Failure(exception) =>
        System.err.println(s"Exception: $exception")
    }
  }

  def printRecentTweetsForUserId(userId: Long, restClient: TwitterRestClient)(implicit executor: ExecutionContext): Unit = {
    restClient.userTimelineForUserId(user_id = userId, count = 5, exclude_replies = false) onComplete {
      case Success(value) =>
        value match {
          case RatedData(rate_limit, data) =>
            System.err.println(rate_limit)
            val tweets = data
            tweets.foreach(tweet => println(s"\n\n$tweet\n\n"))
        }
      case Failure(exception) =>
        System.err.println(s"Exception: $exception")
    }
  }
}
