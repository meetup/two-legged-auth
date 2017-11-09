package com.meetup.auth.util

import java.security.{KeyFactory, KeyPairGenerator}
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.Base64
import org.json4s.JsonDSL._
import prints.Algorithm.Key.Rsa
import prints.{Claims, Header, JWT}

// right now, implementing a pattern that we'll want to pull out into a template or possibly https-oauth2-call monad
/**
 * The OAuth dialog goes like this:
 * 1. consumer requests token Access Token Service
 * 2. with token from 1, make api request
 *
 * //@param privateKey
 * https://tools.ietf.org/html/rfc7523 see section 2.1 "Using JWTs as Authorization Grants", we are doing this
 *
 * as per, https://tools.ietf.org/html/rfc7519#section-4, these claims are required:
 * iss - "issuer", this client, which is requesting
 * sub - "subject", which is memberId in our case
 * aud - "audience", which is the access-token service
 * exp - "expiration", which is UTC time in ms (we set this to timeout 2 minutes ahead of time
 *
 * additionally we provide:
 * TBD
 *
 */
private[auth] class JwtUtil(issuer: String, publicKeyString: String, privateKeyString: String) {

  def createTokenRequestAssertion(memberId: String): Option[String] = {
    val utc: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC) //todo this makes it not a pure function (pass in as param?)

    val header = Header("RS256")
    val claims = Claims(
      ("iss" -> issuer) ~
        ("sub" -> memberId) ~
        ("aud" -> "chapstick") ~
        ("exp" -> utc.plusMinutes(20).toEpochSecond)
    )
    val decoder = Base64.getDecoder
    val keyFactory = KeyFactory.getInstance("RSA")
    val pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(decoder.decode(publicKeyString))).asInstanceOf[RSAPublicKey]
    val privKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoder.decode(privateKeyString))).asInstanceOf[RSAPrivateKey]
    val key = Rsa(pubKey = pubKey, privKey = privKey)

    JWT(header, claims, key).map(new String(_))
  }

  //for testing only, right now
  def validate(jwt: String) = {
    val unrelatedJunkPrivateKeyString = Base64.getEncoder.encodeToString(JwtUtil.generateRSAKeyPair.getPrivate.getEncoded)
    val decoder = Base64.getDecoder
    val keyFactory = KeyFactory.getInstance("RSA")
    val pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(decoder.decode(publicKeyString))).asInstanceOf[RSAPublicKey]
    // we don't use this when reading
    val privKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoder.decode(unrelatedJunkPrivateKeyString))).asInstanceOf[RSAPrivateKey]
    val key = Rsa(pubKey = pubKey, privKey = privKey)
    JWT.verify(jwt, "RS256", key)
  }
}

object JwtUtil {
  //for testing only, right now
  def generateRSAKeyPair = {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(1024)
    generator.generateKeyPair()
  }
}
