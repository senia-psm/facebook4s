package api

import cats.syntax.either._
import config.FacebookConfig._
import org.f100ded.scalaurlbuilder._
import play.api.libs.json._
import domain._
import domain.feed._
import domain.oauth._
import domain.permission.FacebookPermissions._
import services._

import scala.concurrent._

/**
  * Facebook client, api should be used via this object, it provides api methods for your application
  * @param clientId your application id
  * @param appSecret your application secret
  */
class FacebookClient(val clientId: FacebookClientId, val appSecret: FacebookAppSecret)
  extends FacebookInternals {

  import api.FacebookClient._
  import api.FacebookJsonSerializers._
  import uriService._

  /**
    * @return future application access token
    */
  def appAccessToken(): Future[AccessToken] = sendRequestOrFail(appTokenUri)(facebookAppAccessTokenReads)

  /**
    * @param code client code
    * @param machineId optional value that helps to identify specified client
    * @return future long lived user access token
    *         @throws scala.RuntimeException if facebook responds with bad request
    */
  def userAccessToken(code: String, machineId: Option[String] = None): Future[AccessToken] =
    sendRequestOrFail(userTokenUri(code, machineId))(facebookUserAccessTokenReads)

  /**
    * @param longLivedTokenValue long lived token
    * @return future client code
    *         @throws scala.RuntimeException if facebook responds with bad request
    */
  def clientCode(longLivedTokenValue: String): Future[ClientCode] = {
    sendRequestOrFail(accessTokenCodeUri(longLivedTokenValue))(facebookClientCodeReads)
  }

  /**
    * @param shortLivedTokenValue short lived token
    * @return future long lived user access token
    *         @throws scala.RuntimeException if facebook responds with bad request
    */
  def extendUserAccessToken(shortLivedTokenValue: String): Future[AccessToken] =
    sendRequestOrFail(longLivedTokenUri(shortLivedTokenValue))(facebookUserAccessTokenReads)

  /**
    * @param userId Facebook user id
    * @param accessToken User access token
    * @return Facebook user feed
    */
  def feed(userId: UserId, accessToken: AccessToken): Future[UserFeed] =
    sendRequestOrFail(userFeedUri(accessToken, userId))(facebookFeedReads)

  /**
    * @return Either future value of facebook app access token or FacebookOauthError
    */
  def appAccessTokenEither(): AsyncAccessTokenResult = sendRequest(appTokenUri)(facebookAppAccessTokenReads)

  /**
    * @param longLivedTokenValue long lived user access token value
    * @return Either future value of facebook client code or FacebookOauthError
    */
  def clientCodeEither(longLivedTokenValue: String): AsyncClientCodeResult = {
    sendRequest(accessTokenCodeUri(longLivedTokenValue))(facebookClientCodeReads)
  }

  /**
    * @param code client code
    * @param machineId optional value that helps to identify specified client
    * @return Either  future long lived user access token or FacebookOauthError
    */
  def userAccessTokenEither(code: String, machineId: Option[String]): AsyncAccessTokenResult =
    sendRequest(userTokenUri(code, machineId))(facebookUserAccessTokenReads)

  /**
    * @param shortLivedTokenValue short lived user access token
    * @return Either future long lived user access token or FacebookOauthError
    */
  def extendUserAccessTokenEither(shortLivedTokenValue: String): AsyncAccessTokenResult =
    sendRequest(longLivedTokenUri(shortLivedTokenValue))(facebookUserAccessTokenReads)

  /**
    * @param userId Facebook user id
    * @param accessToken Facebook user access token with "user_posts" permission
    * @return Facebook user feed
    */
  def feedEither(userId: UserId, accessToken: AccessToken): AsyncUserFeed =
    sendRequest(userFeedUri(accessToken, userId))(facebookFeedReads)

  /**
    *
    * @param permissions permissions you require for your application
    * @param responseType Determines whether the response data included when the redirect back to the app occurs is in
    *                     URL parameters or fragments. Could be (code, token, code and token or granted_scopes)
    * @param state An arbitrary unique string created by your app to guard against Cross-site Request Forgery
    * @return url that can be used by user of your app log in facebook
    */
  def authUrl(permissions  : Seq[Permissions],
              responseType : ResponseType = FacebookCode,
              state        : Option[String] = None): String =
    uriService.authUrl(permissions, responseType, state).toString()

  private def sendRequest[A](uri: URLBuilder)(reads: Reads[A]) = {
    domainParseService.send(uri)(reads, facebookLoginErrorReads)(loginErrorFE)(appResources)
  }

  private def sendRequestOrFail[A](uri: URLBuilder)(reads: Reads[A]) = {
    domainParseService.sendOrFail(uri)(reads, facebookLoginErrorReads)(loginErrorFE)(appResources)
  }

}

/**
  * Facebook client constructors and helper types
  */
object FacebookClient {

  /**
    * @param clientId your application id
    * @param appSecret your application secret
    * @return backed FacebookClient
    */
  def apply(clientId: FacebookClientId, appSecret: FacebookAppSecret): FacebookClient =
    new FacebookClient(clientId, appSecret)

  /**
    * @param clientIdValue your application id value
    * @param appSecretValue your application secret value
    * @return
    */
  def apply(clientIdValue: String, appSecretValue: String): FacebookClient =
    new FacebookClient(FacebookClientId(clientIdValue), FacebookAppSecret(appSecretValue))

  /**
    * @return FacebookClient created from application id and application secret from type safe config or
    *         OS environmental variables
    */
  def apply(): FacebookClient =
    new FacebookClient(clientId, appSecret)

  /**
    * @param message error message
    * @return Future FacebookOauthError
    */
  def loginErrorFE(message: String) = Future.successful(FacebookOauthError(FacebookError(message)).asLeft)

  type AccessToken = FacebookAccessToken
  type TokenError = FacebookOauthError
  type ClientCode = FacebookClientCode
  type UserFeed = FacebookUserFeed
  type UserId = FacebookUserId
  type Permissions = FacebookUserPermission
  type ResponseType = FacebookOauthResponseType

  type AsyncUserFeed = Future[Either[TokenError, UserFeed]]
  type AsyncAccessTokenResult = Future[Either[TokenError, AccessToken]]
  type AsyncClientCodeResult = Future[Either[TokenError, ClientCode]]
  type FacebookAccessTokenResult = Either[TokenError, AccessToken]
}

