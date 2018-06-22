package com.meetup.auth

import com.meetup.auth.config.Configuration
import com.meetup.auth.util.{AsyncHttpHelper, HttpHelper, JwtUtil}
import com.meetup.logging.Logging
import org.asynchttpclient.Response
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait MeetupConsumer {

  def doClassicApiPost(memberId: String, hostAndPath: String, contentBody: String,
    headers: Map[String, String] = Map())(responseHandler: Option[Response] => Option[String]): Future[Option[String]] //change to type, when we actually do something

  def doClassicApiGet(memberId: String, hostAndPath: String, params: Map[String, String],
    headers: Map[String, String] = Map())(responseHandler: Option[Response] => Option[String]): Future[Option[String]]

  def getAccessTokenOnly(memberId: String): Option[String]
}

/**
 * Provides token access and classic api post/get methods
 *
 * @see [[com.meetup.auth.config.Configuration]]
 */
class MeetupConsumerImpl(configuration: Configuration)(
    httpHelper: HttpHelper = new HttpHelper(),
    asyncHttpHelper: AsyncHttpHelper = AsyncHttpHelper(configuration.userAgent),
    memberId: Option[String] = None) extends MeetupConsumer with Logging {
  val AUTHORIZATION_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer" //https://tools.ietf.org/html/rfc7523#section-2.1
  val jwtUtil = new JwtUtil(
    issuer = configuration.issuer,
    privateKeyString = configuration.privateKey,
    publicKeyString = configuration.publicKey
  )

  lazy val OauthToken: Option[String] = memberId flatMap getAccessTokenOnly

  //todo without a server, this has not been tested
  // though mechanism was tested with:  /ny-scala/?key=3b11f78e28c646b221f69282f3122"
  override def doClassicApiPost(memberId: String, hostAndPath: String, contentBody: String,
    headers: Map[String, String] = Map())(responseHandler: Option[Response] => Option[String] = defaultPostResponseHandler): Future[Option[String]] = {
    val optResultBody = for {
      token <- OauthToken orElse getAccessTokenOnly(memberId) // get access token for memberId  TODO fix this to prevent excess calls
      callResult <- makePostCall(token, hostAndPath, contentBody, headers, responseHandler) // then, make call with token
    } yield callResult
    Future(optResultBody)
  }

  override def doClassicApiGet(memberId: String, hostAndPath: String, params: Map[String, String],
    headers: Map[String, String] = Map())(responseHandler: Option[Response] => Option[String] = defaultGetResponseHandler): Future[Option[String]] = {
    val optResultBody = for {
      token <- OauthToken orElse getAccessTokenOnly(memberId) // get access token for memberId  TODO fix this to prevent excess calls
      callResult <- makeGetCall(token, hostAndPath, params, headers, responseHandler) // then, make call with token
    } yield callResult
    Future(optResultBody)
  }

  private[auth] def makePostCall(token: String, hostAndPath: String, contentBody: String, otherHeaders: Map[String, String],
    responseHandler: Option[Response] => Option[String]): Option[String] = {
    val headers = Map("Authorization" -> s"Bearer $token") ++ otherHeaders

    optHttpsPostAHC(
      hostAndPath = hostAndPath,
      contentBody = contentBody,
      headers = headers,
      responseHandler = responseHandler
    )
  }

  private[auth] def makeGetCall(token: String, hostAndPath: String, params: Map[String, String],
    moreHeaders: Map[String, String], responseHandler: Option[Response] => Option[String]): Option[String] = {
    val headers = Map("Authorization" -> s"Bearer $token") ++ moreHeaders

    optHttpsGet(
      hostAndPath = hostAndPath,
      params = params,
      headers = headers,
      responseHandler = responseHandler
    )
  }

  private[auth] def optHttpsPost(hostAndPath: String, contentBody: String,
    moreHeaders: Map[String, String]): Option[String] = {
    val opt = Try(httpHelper.sendOldSchoolHttpsPost(hostAndPath, contentBody, moreHeaders))
    if (opt.isFailure) {
      opt.failed.map(th => {
        log.error(
          s"failed to send POST: $hostAndPath\n" +
            s"with contentBody:\n    $contentBody\n" +
            s"and headers:\n    $moreHeaders\n" +
            s"and error: ${th.getMessage}\nand trace:\n"
        )
        th.printStackTrace()
      })
    }
    opt.toOption
  }

  /**
   * Using AsyncHttpClient
   */
  private[auth] def optHttpsPostAHC(hostAndPath: String, contentBody: String, headers: Map[String, String] = Map(),
    responseHandler: Option[Response] => Option[String] = defaultPostResponseHandler): Option[String] = {
    val opt = Try(asyncHttpHelper.asyncPostWithBody(hostAndPath, contentBody, headers).get())
    if (opt.isFailure) {
      opt.failed.map(th => {
        log.error(
          s"failed to send POST: $hostAndPath\n" +
            s"with contentBody:\n    $contentBody\n" +
            s"and headers:\n    $headers\n" +
            s"and error: ${th.getMessage}\nand trace:\n"
        )
        th.printStackTrace()
      })
    }

    val resp = opt.toOption
    responseHandler(resp)
  }

  private[auth] val defaultGetResponseHandler: Option[Response] => Option[String] = defaultResponseHandler("GET")
  private[auth] val defaultPostResponseHandler: Option[Response] => Option[String] = defaultResponseHandler("POST")

  private[auth] def defaultResponseHandler(methodType: String)(responseOpt: Option[Response]) = {
    responseOpt match {
      case None => None
      case Some(r) if r.getStatusCode >= 400 =>
        log.error(s"received non-200 response from $methodType\n" +
          s"and http status code: ${r.getStatusCode}\n" +
          s"and body: ${r.getResponseBody}\n")
        None
      case r => r.map(_.getResponseBody)
    }
  }

  private[auth] def optHttpsGet(hostAndPath: String, params: Map[String, String], headers: Map[String, String] = Map(),
    responseHandler: Option[Response] => Option[String] = defaultGetResponseHandler): Option[String] = {
    val opt = Try(asyncHttpHelper.asyncGet(hostAndPath, params, headers).get())
    if (opt.isFailure) {
      opt.failed.map(th => {
        log.error(
          s"failed to send GET: $hostAndPath\n" +
            s"with params:\n    $params\n" +
            s"and headers:\n    $headers\n" +
            s"and error: ${th.getMessage}\nand trace:\n"
        )
        th.printStackTrace()
      })
    }

    val resp = opt.toOption
    responseHandler(resp)
  }

  override def getAccessTokenOnly(memberId: String): Option[String] = {
    val content = s"client_id=${configuration.classicOAuthClientKey}" +
      s"&grant_type=$AUTHORIZATION_GRANT_TYPE" +
      s"&assertion=${jwtUtil.createTokenRequestAssertion(memberId).getOrElse("")}"

    log.info(content)
    val opt = optHttpsPostAHC(s"https://${configuration.accessTokenUrl}", content)
    val token = for {
      json <- opt
      JObject(listOfFields) <- parseOpt(json)
      (key, value) <- listOfFields.find(t => t._1.equalsIgnoreCase("access_token"))
      JString(tk) <- value.toOption
    } yield tk
    token.foreach { _ => // if there is a token
      Thread.sleep(500) // Chapstick may have token replication issue, and using it immediately may fail
    }
    token
  }
}

object MeetupConsumer {
  def apply(configuration: Configuration) = new MeetupConsumerImpl(configuration)()
}
