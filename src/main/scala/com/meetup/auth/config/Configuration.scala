package com.meetup.auth.config

import scala.util.Properties._
import scala.util.control.Exception.allCatch

/**
 * Configuration for 2-legged authentication.
 * @param issuer Client name, which is requesting, used for token assertion (example "app-service")
 * @param userAgent User-Agent header param used for post/get requests (example "APP/0.1")
 * @param privateKey RSA256 private key base64-encoded
 * @param publicKey public key corresponding with above
 * @param classicOAuthClientKey oauth client_key as registered in Meetup Classic
 * @param accessTokenUrl where to post request to get a new accessToken
 * @param apiRootUrl Classic base url including protocol. no trailing "/" recommended
 */
case class Configuration(issuer: String, userAgent: String, privateKey: String, publicKey: String,
  classicOAuthClientKey: String, accessTokenUrl: String, apiRootUrl: String)

object Configuration {

  //Creating a parser to handle config types. There must be a lib, but this is clear enough
  private[auth] trait Parser[T] {
    def parse(input: String): Option[T]
  }

  private[auth] def parse[T](input: String)(implicit parser: Parser[T]): Option[T] = parser.parse(input)

  private[auth] implicit object StringParser extends Parser[String] {
    def parse(input: String) = Some(input.trim)
    Configuration
  }

  private[auth] implicit object BooleanParser extends Parser[Boolean] {
    def parse(input: String) = allCatch.opt(input.toBoolean)
  }

  /**
   * Instantiates Configuration according to provided environment variable tags
   * Specify issuer, userAgent explicitly regarding the application.
   * @param issuer Client name, which is requesting, used for token assertion (example "pro-bcan-service")
   * @param userAgent User-Agent header param used for post/get requests (example "BACN/0.1")
   * @param privateKeyTag env variable name for RSA256 private key base64-encoded
   * @param publicKeyTag env variable name for public key corresponding with above
   * @param classicOAuthClientKeyTag env variable name for oauth client_key as registered in Meetup Classic
   * @param accessTokenUrlTag env variable name for where to post request to get a new accessToken
   * @param apiRootUrlTag env variable name for classic base url including protocol. no trailing "/" recommended
   *
   * @example {{{ loadConfigurationFromEnv(issuer = "app-service", userAgent = "APP/0.1")
   *          ("APP_PRIVATE_KEY",
   *           "APP_PUBLIC_KEY",
   *           "APP_CLASSIC_OAUTH2_CLIENT_KEY",
   *           "APP_CHAPSTICK_ACCESS_TOKEN_URL",
   *           "APP_CHAPSTICK_API_ROOT_URL") }}}
   */
  def loadConfigurationFromEnv(issuer: String, userAgent: String)(privateKeyTag: String, publicKeyTag: String,
    classicOAuthClientKeyTag: String, accessTokenUrlTag: String, apiRootUrlTag: String): Configuration =
    new Configuration(
      issuer = issuer,
      userAgent = userAgent,
      privateKey = resolutionOrderOrElse(privateKeyTag, "bad-private-key, none in environment"),
      publicKey = resolutionOrderOrElse(publicKeyTag, "bad-public-key, none in environment"),
      classicOAuthClientKey = resolutionOrderOrElse(classicOAuthClientKeyTag, "bad-oauth-client-key, none in environment"),
      accessTokenUrl = resolutionOrderOrElse(accessTokenUrlTag, "secure.dev.meetup.com/oauth2/access"),
      apiRootUrl = resolutionOrderOrElse(apiRootUrlTag, "api.dev.meetup.com")
    )

  // first try to load property from scala-java Properties,
  // then try to load from environment,
  // finally, use "default" string passed in as parameter
  private[auth] def resolutionOrderOrElse[T: Parser](propertyName: String, default: T): T = {
    val propValue: Option[String] = propOrNone(propertyName)
      .orElse(envOrNone(propertyName))
    propValue.flatMap(parse[T]).getOrElse(default)
  }
}
