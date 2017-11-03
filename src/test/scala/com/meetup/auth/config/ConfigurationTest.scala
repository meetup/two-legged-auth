package com.meetup.auth.config

import org.scalatest.{FunSpec, GivenWhenThen}

import scala.util.Properties

class ConfigurationTest extends FunSpec with GivenWhenThen {
  val AUTH_CONFIGURATION_TEST_PROP = "AUTH_CONFIGURATION_TEST_PROP"
  describe("resolving configuration") {

    it("should get from property, if exists") {
      Given("environment is empty and there is a scala Property set")
      assert(sys.env.get(AUTH_CONFIGURATION_TEST_PROP) === None)
      Properties.setProp(AUTH_CONFIGURATION_TEST_PROP, "test value")

      When("property resolution is attempted")
      val result = Configuration.resolutionOrderOrElse(AUTH_CONFIGURATION_TEST_PROP, "default value")

      Then("set Property was detected and returned")
      assert(result === "test value")
      assert(result !== "default value")
    }

    it("should get from env, if property doesn't exist, and env exists") {
      //not currently tested because sys.env is read only in scala. needs integration test
      // tested manually by setting env in IDEA run config and seeing specific Some("env value") failure of test below
    }

    it("should get set to default values, if doesn't exist either as property, nor in env") {
      Given("environment is empty and scala Properties empty")
      assert(sys.env.get(AUTH_CONFIGURATION_TEST_PROP) === None)
      Properties.clearProp(AUTH_CONFIGURATION_TEST_PROP)

      When("property resolution is attempted")
      val result = Configuration.resolutionOrderOrElse(AUTH_CONFIGURATION_TEST_PROP, "default value")

      Then("set Property was detected and returned")
      assert(result !== "test value")
      assert(result === "default value")
    }

    it("should handle boolean values fine") {
      Given("environment is empty and there is a scala Property set")
      assert(sys.env.get(AUTH_CONFIGURATION_TEST_PROP) === None)
      Properties.setProp(AUTH_CONFIGURATION_TEST_PROP, "true")
      When("property resolution is attempted")
      val result = Configuration.resolutionOrderOrElse(AUTH_CONFIGURATION_TEST_PROP, false)

      Then("set Property was detected and returned")
      assert(result === true)
    }
  }
}
