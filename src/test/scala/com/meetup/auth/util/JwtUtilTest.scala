package com.meetup.auth.util

import java.security.KeyPairGenerator
import java.util.Base64

import org.scalatest.{FunSpec, GivenWhenThen}

class JwtUtilTest extends FunSpec with GivenWhenThen {
  describe("Oauth2 conversation depends on package-private helper functions to do work") {
    it("should produce usable jwt") {
      Given("a valid RSA private and public key in setup configuration")
      val generator = KeyPairGenerator.getInstance("RSA")
      generator.initialize(1024)
      val keypair = generator.generateKeyPair()
      val privKey = Base64.getEncoder.encodeToString(keypair.getPrivate.getEncoded)
      val pubKey = Base64.getEncoder.encodeToString(keypair.getPublic.getEncoded)
      val jwtUtil = new JwtUtil("pro-bcan-service", privateKeyString = privKey, publicKeyString = pubKey)

      When("jwt assertion is created")
      val assertionOpt: Option[String] = jwtUtil.createTokenRequestAssertion("dummyId")

      Then("assertion string is validated")
      assert(assertionOpt
        .map(assertion => jwtUtil.validate(assertion).isRight) // if not valid, would return return false
        .exists(a => a))
    }
  }
}
