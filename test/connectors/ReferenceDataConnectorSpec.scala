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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import connectors.ReferenceDataConnectorSpec._
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.http.Status.NO_CONTENT
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private def checkErrorResponse(url: String, result: Future[_]): Assertion = {
    val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)
    forAll(errorResponseCodes) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url)).willReturn(aResponse().withStatus(errorResponse))
        )

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

  private val queryParams = Seq("foo" -> "bar")

  "Reference Data" - {

    "GET" - {

      "getCustomsOffice" - {

        val url = s"$baseUrl/filtered-lists/CustomsOffices?foo=bar"

        "should handle a 200 response for customs offices" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(customsOfficesResponseJson))
          )

          val expectedResult = Seq(CustomsOffice(code, "NAME001", Some("004412323232345")))

          connector.getCustomsOffices(queryParams).futureValue mustBe expectedResult
        }

        "should handle a 204 response for customs offices" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          connector.getCustomsOffices(queryParams).futureValue mustBe Nil
        }

        "should handle client and server errors for customs offices" in {
          checkErrorResponse(url, connector.getCustomsOffices(queryParams))
        }
      }

      "getControlType" - {

        val url = s"$baseUrl/filtered-lists/ControlType?foo=bar"

        "should handle a 200 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(controlTypesResponseJson))
          )

          val expectedResult = Seq(ControlType(typeOfControl, "Intrusive"))

          connector.getControlTypes(queryParams).futureValue mustBe expectedResult
        }

        "should handle a 204 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          connector.getControlTypes(queryParams).futureValue mustBe Nil
        }

        "should handle client and server errors for control types" in {
          checkErrorResponse(url, connector.getControlTypes(queryParams))
        }
      }

      "getFunctionalErrors" - {

        "when filtering" - {

          val url = s"$baseUrl/filtered-lists/FunctionalErrorCodesIeCA?foo=bar"

          "should handle a 200 response for functional errors" in {
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(okJson(functionalErrorsResponseJson))
            )

            val expectedResult = Seq(FunctionalErrorWithDesc(functionalError, "Rule violation"))

            connector.getFunctionalErrors(queryParams).futureValue mustBe expectedResult
          }

          "should handle a 204 response for functional errors" in {
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(NO_CONTENT))
            )

            connector.getFunctionalErrors(queryParams).futureValue mustBe Nil
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

            val expectedResult = Seq(FunctionalErrorWithDesc(functionalError, "Rule violation"))

            connector.getFunctionalErrors().futureValue mustBe expectedResult
          }

          "should handle a 204 response for functional errors" in {
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(NO_CONTENT))
            )

            connector.getFunctionalErrors().futureValue mustBe Nil
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

  private val code            = "GB00001"
  private val typeOfControl   = "44"
  private val functionalError = "14"

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
}
