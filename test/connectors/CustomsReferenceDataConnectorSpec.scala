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
import connectors.CustomsReferenceDataConnectorSpec._
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.http.Status.NO_CONTENT
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  private lazy val connector: CustomsReferenceDataConnector = app.injector.instanceOf[CustomsReferenceDataConnector]

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

  "Customs Reference Data" - {

    "GET" - {

      "getCustomsOffice" - {

        val url = s"$customsOfficeUri?data.id=$code"

        "should handle a 200 response for customs offices" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(customsOfficesResponseJson))
          )

          val expectedResult = CustomsOffice(code, "NAME001", Some("004412323232345"))

          connector.getCustomsOffice(code).futureValue.value mustBe expectedResult
        }

        "should handle a 204 response for customs offices" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          connector.getCustomsOffice(code).futureValue mustBe None
        }

        "should handle client and server errors for customs offices" in {
          checkErrorResponse(url, connector.getCustomsOffice(code))
        }
      }

      "getControlType" - {

        val url = s"$controlTypeUri?data.code=$typeOfControl"

        "should handle a 200 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(controlTypesResponseJson))
          )

          val expectedResult = ControlType(typeOfControl, "Intrusive")

          connector.getControlType(typeOfControl).futureValue.value mustBe expectedResult
        }

        "should handle a 204 response for control types" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          connector.getControlType(typeOfControl).futureValue mustBe None
        }

        "should handle client and server errors for control types" in {
          checkErrorResponse(url, connector.getControlType(typeOfControl))
        }
      }

      "getFunctionalErrorDescription" - {

        val url = s"$functionalErrorUri?data.code=$functionalError"

        "should handle a 200 response for functional errors" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(okJson(functionalErrorsResponseJson))
          )

          val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

          connector.getFunctionalErrorDescription(functionalError).futureValue.value mustBe expectedResult
        }

        "should handle a 204 response for functional errors" in {
          server.stubFor(
            get(urlEqualTo(url))
              .willReturn(aResponse().withStatus(NO_CONTENT))
          )

          connector.getFunctionalErrorDescription(functionalError).futureValue mustBe None
        }

        "should handle client and server errors for functional errors" in {
          checkErrorResponse(url, connector.getFunctionalErrorDescription(functionalError))
        }
      }
    }
  }
}

object CustomsReferenceDataConnectorSpec {

  private val code            = "GB00001"
  private val typeOfControl   = "44"
  private val functionalError = "14"

  private val baseUrl            = "/customs-reference-data/test-only"
  private val customsOfficeUri   = s"$baseUrl/filtered-lists/CustomsOffices"
  private val controlTypeUri     = s"$baseUrl/filtered-lists/ControlType"
  private val functionalErrorUri = s"$baseUrl/filtered-lists/FunctionalErrorCodesIeCA"

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
