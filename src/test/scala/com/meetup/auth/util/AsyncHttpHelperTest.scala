package com.meetup.auth.util

import org.asynchttpclient.AsyncHttpClient
import org.mockito.Mockito.{when => mockWhen}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen}

import scala.collection.JavaConverters._

class AsyncHttpHelperTest extends FunSpec with GivenWhenThen with MockitoSugar {
  describe("AsyncHttpHelper") {
    it("creates GET request") {
      Given("required info for GET request")
      val client = mock[AsyncHttpClient]
      val helper = new AsyncHttpHelper(client, "BCAN/0.1")
      val url = "http://nyan.cat"
      val params = Map("Leroy" -> "Jenkins")
      val headers = Map("Foo" -> "Bar")

      When("createGetRequest() is invoked")
      val result = helper.createGetRequest(url, params, headers)

      Then("The result contains all the originally given info")
      assert(result.getMethod == "GET")
      assert(result.getUrl == url + "?" + params.toList.map(p => s"${p._1}=${p._2}").mkString("&"))
      assert(result.getQueryParams.asScala.exists(p => p.getName == "Leroy" && p.getValue == "Jenkins"))
      assert(result.getHeaders.contains("Foo", "Bar", false))
    }

    it("creates POST request with form params") {
      Given("required info for POST request")
      val client = mock[AsyncHttpClient]
      val helper = new AsyncHttpHelper(client, "BCAN/0.1")
      val url = "http://nyan.cat"
      val params = Map("Leroy" -> "Jenkins")
      val headers = Map("Foo" -> "Bar")

      When("createGetRequest() is invoked")
      val result = helper.createPostRequest(url, params, headers)

      Then("The result contains all the originally given info")
      assert(result.getMethod == "POST")
      assert(result.getUrl == url)
      assert(result.getFormParams.asScala.exists(p => p.getName == "Leroy" && p.getValue == "Jenkins"))
      assert(result.getHeaders.contains("Foo", "Bar", false))
    }

    it("creates POST request with content body") {
      Given("required info for POST request")
      val client = mock[AsyncHttpClient]
      val helper = new AsyncHttpHelper(client, "BCAN/0.1")
      val url = "http://nyan.cat"
      val body = "Leroy=Jenkins"
      val headers = Map("Foo" -> "Bar")

      When("createGetRequest() is invoked")
      val result = helper.createPostRequestWithBody(url, body, headers)

      Then("The result contains all the originally given info")
      assert(result.getMethod == "POST")
      assert(result.getUrl == url)
      assert(result.getStringData == body)
      assert(result.getHeaders.contains("Foo", "Bar", false))
    }
  }

}
