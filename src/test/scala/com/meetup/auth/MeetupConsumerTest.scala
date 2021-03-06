package com.meetup.auth

import com.meetup.auth.config.Configuration
import com.meetup.auth.util.AsyncHttpHelper
import org.asynchttpclient.{ListenableFuture, Response}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{when => mockWhen}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen}

class MeetupConsumerTest extends FunSpec with GivenWhenThen with MockitoSugar {
  val configuration = Configuration("mock", "mock", "mock", "mock", "mock", "mock", "mock")

  describe("MeetupConsumer") {
    describe("optHttpsPostAHC") {
      it("use custom response handler") {
        When("a non-200 request is made")
        val response = mock[Response]
        mockWhen(response.getStatusCode).thenReturn(409)
        mockWhen(response.getResponseBody).thenReturn("Tons of conflicts =_=")
        val futureResponse = mock[ListenableFuture[Response]]
        mockWhen(futureResponse.get()).thenReturn(response)
        val httpHelper = mock[AsyncHttpHelper]
        mockWhen(httpHelper.asyncPostWithBody(any[String], any[String], any[Map[String, String]])).thenReturn(futureResponse)
        val consumer = new MeetupConsumerImpl(configuration = configuration)(asyncHttpHelper = httpHelper)
        val messageFromHandler = "Sorry, error occured."
        val result = consumer.optHttpsPostAHC("path", "body", Map.empty, _ => Some(messageFromHandler))
        Then("the failed response is handled")
        assert(result.contains(messageFromHandler))
      }

      it("works for successful response") {
        Given("the request info")
        val url = "https://nyan.cat"
        val formBody = "Leroy Jenkins!!!"
        val headers = Map("Foo" -> "Bar")

        When("a successful request is made")
        val response = mock[Response]
        mockWhen(response.getStatusCode).thenReturn(200)
        mockWhen(response.getResponseBody).thenReturn("It works!")
        val futureResponse = mock[ListenableFuture[Response]]
        mockWhen(futureResponse.get()).thenReturn(response)
        val httpHelper = mock[AsyncHttpHelper]
        mockWhen(httpHelper.asyncPostWithBody(any[String], any[String], any[Map[String, String]])).thenReturn(futureResponse)
        val consumer = new MeetupConsumerImpl(configuration = configuration)(asyncHttpHelper = httpHelper)
        val result = consumer.optHttpsPostAHC(url, formBody, headers)

        Then("the successful response is fetched")
        assert(result.contains("It works!"))
      }
    }

    describe("optHttpsGet") {
      it("works for successful response") {
        Given("the request info")
        val url = "https://nyan.cat"
        val params = Map("this" -> "that")
        val headers = Map("Foo" -> "Bar")

        When("a successful request is made")
        val response = mock[Response]
        mockWhen(response.getStatusCode).thenReturn(200)
        mockWhen(response.getResponseBody).thenReturn("It works!")
        val futureResponse = mock[ListenableFuture[Response]]
        mockWhen(futureResponse.get()).thenReturn(response)
        val httpHelper = mock[AsyncHttpHelper]
        mockWhen(httpHelper.asyncGet(any[String], any[Map[String, String]], any[Map[String, String]])).thenReturn(futureResponse)
        val consumer = new MeetupConsumerImpl(configuration = configuration)(asyncHttpHelper = httpHelper)
        val result = consumer.optHttpsGet(url, params, headers)

        Then("the successful response is fetched")
        assert(result.contains("It works!"))
      }
    }

    describe("optHttpsPatchAHC") {
      it("use custom response handler") {
        When("a non-200 request is made")
        val response = mock[Response]
        mockWhen(response.getStatusCode).thenReturn(409)
        mockWhen(response.getResponseBody).thenReturn("Tons of conflicts =_=")
        val futureResponse = mock[ListenableFuture[Response]]
        mockWhen(futureResponse.get()).thenReturn(response)
        val httpHelper = mock[AsyncHttpHelper]
        mockWhen(httpHelper.asyncPatchWithBody(any[String], any[String], any[Map[String, String]])).thenReturn(futureResponse)
        val consumer = new MeetupConsumerImpl(configuration = configuration)(asyncHttpHelper = httpHelper)
        val messageFromHandler = "Sorry, error occured."
        val result = consumer.optHttpsPatchAHC("path", "body", Map.empty, _ => Some(messageFromHandler))
        Then("the failed response is handled")
        assert(result.contains(messageFromHandler))
      }

      it("works for successful response") {
        Given("the request info")
        val url = "https://nyan.cat"
        val formBody = "Leroy Jenkins!!!"
        val headers = Map("Foo" -> "Bar")

        When("a successful request is made")
        val response = mock[Response]
        mockWhen(response.getStatusCode).thenReturn(200)
        mockWhen(response.getResponseBody).thenReturn("It works!")
        val futureResponse = mock[ListenableFuture[Response]]
        mockWhen(futureResponse.get()).thenReturn(response)
        val httpHelper = mock[AsyncHttpHelper]
        mockWhen(httpHelper.asyncPatchWithBody(any[String], any[String], any[Map[String, String]])).thenReturn(futureResponse)
        val consumer = new MeetupConsumerImpl(configuration = configuration)(asyncHttpHelper = httpHelper)
        val result = consumer.optHttpsPatchAHC(url, formBody, headers)

        Then("the successful response is fetched")
        assert(result.contains("It works!"))
      }
    }
  }

}
