/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec._
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, RequestedDocumentType}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[_]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ mustBe a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady[Throwable, Assertion](result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

  private val queryParams = "foo" -> "bar"

  "Reference Data" - {

    "GET" - {

      "getCustomsOffice" - {

        val url = s"$baseUrl/lists/CustomsOffices?foo=bar"

        "should handle a 200 response for customs offices" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(customsOfficesResponseJson))
          )

          val expectedResult = NonEmptySet.of(CustomsOffice(code, "NAME001", Some("004412323232345")))

          connector.getCustomsOffices(queryParams).futureValue mustBe expectedResult
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getCustomsOffices(queryParams))
        }

        "should handle client and server errors for customs offices" in {
          checkErrorResponse(url, connector.getCustomsOffices(queryParams))
        }
      }

      "getControlType" - {

        val url = s"$baseUrl/lists/ControlType?foo=bar"

        "should handle a 200 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(controlTypesResponseJson))
          )

          val expectedResult = NonEmptySet.of(ControlType(typeOfControl, "Intrusive"))

          connector.getControlTypes(queryParams).futureValue mustBe expectedResult
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getControlTypes(queryParams))
        }

        "should handle client and server errors for control types" in {
          checkErrorResponse(url, connector.getControlTypes(queryParams))
        }
      }

      "getRequestedDocumentTypes" - {

        val url = s"$baseUrl/lists/RequestedDocumentType?foo=bar"

        "should handle a 200 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(requestedDocumentTypeJson))
          )

          val expectedResult = NonEmptySet.of(RequestedDocumentType("C620", "T2FL document"))

          connector.getRequestedDocumentTypes(queryParams).futureValue mustBe expectedResult
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, connector.getRequestedDocumentTypes(queryParams))
        }

        "should handle client and server errors for control types" in {
          checkErrorResponse(url, connector.getRequestedDocumentTypes(queryParams))
        }
      }

      "getFunctionalErrors" - {

        "when filtering" - {

          val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA?foo=bar"

          "should handle a 200 response for functional errors" in {
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(okJson(functionalErrorsResponseJson))
            )

            val expectedResult = NonEmptySet.of(FunctionalErrorWithDesc(functionalError, "Rule violation"))

            connector.getFunctionalErrors(queryParams).futureValue mustBe expectedResult
          }

          "should throw a NoReferenceDataFoundException for an empty response" in {
            checkNoReferenceDataFoundResponse(url, connector.getFunctionalErrors(queryParams))
          }

          "should handle client and server errors for functional errors" in {
            checkErrorResponse(url, connector.getFunctionalErrors(queryParams))
          }
        }

        "when not filtering" - {

          val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA"

          "should handle a 200 response for functional errors" in {
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(okJson(functionalErrorsResponseJson))
            )

            val expectedResult = NonEmptySet.of(FunctionalErrorWithDesc(functionalError, "Rule violation"))

            connector.getFunctionalErrors().futureValue mustBe expectedResult
          }

          "should throw a NoReferenceDataFoundException for an empty response" in {
            checkNoReferenceDataFoundResponse(url, connector.getFunctionalErrors())
          }

          "should handle client and server errors for functional errors" in {
            checkErrorResponse(url, connector.getFunctionalErrors())
          }
        }
      }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val code                  = "GB00001"
  private val typeOfControl         = "44"
  private val requestedDocumentType = "C620"
  private val functionalError       = "14"

  private val baseUrl = "/customs-reference-data/test-only"

  private val customsOfficesResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "id": "$code",
       |      "name": "NAME001",
       |      "phoneNumber": "004412323232345"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val controlTypesResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "code": "$typeOfControl",
       |      "description": "Intrusive"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val requestedDocumentTypeJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "code": "$requestedDocumentType",
       |      "description": "T2FL document"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val functionalErrorsResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "code": "$functionalError",
       |      "description": "Rule violation"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val emptyResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin
}
