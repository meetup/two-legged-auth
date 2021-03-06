package com.meetup.auth.util

import scala.collection.JavaConverters._

import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.ssl.{SslContextBuilder, SslProvider}
import org.asynchttpclient._

object AsyncHttpHelper {
  def apply(userAgent: String): AsyncHttpHelper = new AsyncHttpHelper(client.get(), userAgent)

  val ConnectionTimeoutMillis = 5000

  private[this] val SslContext = SslContextBuilder
    .forClient()
    .sslProvider(SslProvider.JDK)
    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    .build()

  private[this] val Config = new DefaultAsyncHttpClientConfig.Builder()
    .setSslContext(SslContext)
    .setConnectTimeout(ConnectionTimeoutMillis)
    .build()

  private[util] val client = Lazy(new DefaultAsyncHttpClient(Config))
}

/**
 * Get/Post http request build helper.
 *
 */
class AsyncHttpHelper(client: AsyncHttpClient, userAgent: String) {

  def asyncGet(url: String, params: Map[String, String], headers: Map[String, String]): ListenableFuture[Response] = {
    client.executeRequest(createGetRequest(url, params, headers))
  }

  def createGetRequest(url: String, params: Map[String, String], otherHeaders: Map[String, String]): Request = {
    val headers = Map("User-Agent" -> userAgent)
    val adjustedHeaders = headers ++ otherHeaders
    val request = new RequestBuilder()
      .setMethod("GET")
      .setUrl(url)
      .setQueryParams(params.mapValues(v => List(v).asJava).asJava)
      .setSingleHeaders(adjustedHeaders.asJava)
      .build()
    request
  }

  def asyncPostWithBody(url: String, contentBody: String, headers: Map[String, String]): ListenableFuture[Response] = {
    client.executeRequest(createPostRequestWithBody(url, contentBody, headers))
  }

  def createPostRequestWithBody(url: String, contentBody: String, otherHeaders: Map[String, String]): Request = {
    val headers = Map(
      "User-Agent" -> userAgent,
      "Content-Type" -> "application/x-www-form-urlencoded",
      "Content-Length" -> s"${contentBody.length}"
    )

    val adjustedHeaders = headers ++ otherHeaders

    val request = new RequestBuilder()
      .setMethod("POST")
      .setUrl(url)
      .setBody(contentBody)
      .setSingleHeaders(adjustedHeaders.asJava)
      .build()
    request
  }

  def asyncPost(url: String, params: Map[String, String], headers: Map[String, String]): ListenableFuture[Response] = {
    client.executeRequest(createPostRequest(url, params, headers))
  }

  def createPostRequest(url: String, params: Map[String, String], headers: Map[String, String]): Request = {
    val adjustedHeaders = headers +
      ("User-Agent" -> userAgent) +
      ("Content-Type" -> "application/x-www-form-urlencoded")
    val request = new RequestBuilder()
      .setMethod("POST")
      .setUrl(url)
      .setFormParams(params.mapValues(v => List(v).asJava).asJava)
      .setSingleHeaders(adjustedHeaders.asJava)
      .build()
    request
  }

  def asyncPatchWithBody(url: String, contentBody: String, headers: Map[String, String]): ListenableFuture[Response] = {
    client.executeRequest(createPatchRequestWithBody(url, contentBody, headers))
  }

  def createPatchRequestWithBody(url: String, contentBody: String, otherHeaders: Map[String, String]): Request = {
    val headers = Map(
      "User-Agent" -> userAgent,
      "Content-Type" -> "application/x-www-form-urlencoded",
      "Content-Length" -> s"${contentBody.length}"
    )

    val adjustedHeaders = headers ++ otherHeaders

    val request = new RequestBuilder()
      .setMethod("PATCH")
      .setUrl(url)
      .setBody(contentBody)
      .setSingleHeaders(adjustedHeaders.asJava)
      .build()
    request
  }
}
