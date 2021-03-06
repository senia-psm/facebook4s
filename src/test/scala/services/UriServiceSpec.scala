package services

import cats.implicits._
import config.FacebookConfig._
import domain.oauth.{FacebookAccessToken, FacebookToken, TokenValue, UserAccessToken}
import domain.permission.FacebookPermissions.FacebookUserPosts
import org.scalatest.{Matchers, WordSpec}
import other.FacebookShowOps._

import scala.concurrent.duration.DurationInt

class UriServiceSpec extends WordSpec with Matchers {

  val s = UriService()

  "UriService" should {
    "return url to obtain log lived uri" in {
      s.longLivedTokenUri("test").toString() shouldBe "https://graph.facebook.com/v2.10/oauth/access_token" +
        s"?client_id=${clientId.show}" +
        s"&client_secret=${appSecret.show}" +
        s"&grant_type=fb_exchange_token" +
        s"&fb_exchange_token=test"
    }

    "return post uri" in {
      s.postUri("postId", FacebookAccessToken(
        TokenValue("token"), UserAccessToken("", 1000.seconds))).toString() shouldBe "https://graph.facebook.com" +
        "/v2.10/postId?access_token=token"
    }

    "return auth uri" in {
      s.authUrl(Seq.empty).toString() shouldBe s"https://facebook.com/v2.10/dialog/oauth" +
        s"?client_id=${clientId.show}" +
        s"&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fredirect" +
        s"&response_type=code"
    }

    "return auth uri with state, response_type and permissions" in {
      s.authUrl(
        permissions  = Seq(FacebookUserPosts),
        responseType = FacebookToken,
        state = Some("asd")).toString() shouldBe s"https://facebook.com/v2.10/dialog/oauth" +
        s"?client_id=${clientId.show}" +
        s"&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fredirect" +
        s"&response_type=${FacebookToken.value}" +
        s"&scope=${FacebookUserPosts.value}" +
        s"&state=asd"
    }
  }

}
