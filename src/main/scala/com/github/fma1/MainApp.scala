package com.github.fma1

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import scala.concurrent.ExecutionContext.Implicits.global

object MainApp extends App {
  val consumerToken: ConsumerToken = _
  val accessToken: AccessToken = _

  val restClient: TwitterRestClient = TwitterRestClient(consumerToken, accessToken)

  val userId = 1260597074036355072L

  Utils.printRecentTweetsForUserId(userId, restClient)

  Thread.sleep(200000)
}
