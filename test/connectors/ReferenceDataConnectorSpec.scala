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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockSuite {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.reference-data.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]
  val code                                           = "GB00001"
  val typeOfControl                                  = "44"

  "Reference Data" - {

    "GET" - {

      "getCustomsOffice" - {

        "should handle a 200 response for customs office with code end point with valid phone number" in {
          server.stubFor(
            get(urlEqualTo(s"$customsOfficeUri/$code"))
              .willReturn(okJson(customsOfficeResponseJsonWithPhone))
          )

          val expectedResult = Some(CustomsOffice("ID1", "NAME001", Some("004412323232345")))

          connector.getCustomsOffice(code).futureValue mustBe expectedResult
        }

        "should handle a 200 response for customs office with code end point with no phone number" in {
          server.stubFor(
            get(urlEqualTo(s"$customsOfficeUri/$code"))
              .willReturn(okJson(customsOfficeResponseJsonWithOutPhone))
          )

          val expectedResult = Some(CustomsOffice("ID1", "NAME001", None))

          connector.getCustomsOffice("GB00001").futureValue mustBe expectedResult
        }
        "should handle client and server errors for customs office end point" in {
          val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

          forAll(errorResponseCodes) {
            errorResponse =>
              server.stubFor(
                get(urlEqualTo(s"$customsOfficeUri/$code"))
                  .willReturn(
                    aResponse()
                      .withStatus(errorResponse)
                  )
              )

              connector.getCustomsOffice("GB00001").futureValue mustBe None
          }
        }
      }

      "getControlType" - {

        "should handle a 200 response for control types" in {
          server.stubFor(
            get(urlEqualTo(s"$controlTypeUri"))
              .willReturn(okJson(controlType))
          )

          val expectedResult = ControlType("44", "Intrusive")

          connector.getControlType(typeOfControl).futureValue mustBe expectedResult
        }

        "should handle client and server errors for control type end point" in {
          val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

          forAll(errorResponseCodes) {
            errorResponse =>
              server.stubFor(
                get(urlEqualTo(s"$controlTypeUri"))
                  .willReturn(
                    aResponse()
                      .withStatus(errorResponse)
                  )
              )

              connector.getControlType(typeOfControl).futureValue mustBe ControlType(typeOfControl, "")
          }
        }
      }

      "getFunctionalErrorDescription" - {

        "should handle a 200 response for functional errors" in {
          server.stubFor(
            get(urlEqualTo(s"$functionalErrorUri"))
              .willReturn(okJson(functionalErrorJson))
          )

          val expectedResult = FunctionalErrorWithDesc("14", "Rule violation")

          connector.getFunctionalErrorDescription(functionalError).futureValue mustBe expectedResult
        }

        "should handle client and server errors for control type end point" in {
          val errorResponseCodes: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

          forAll(errorResponseCodes) {
            errorResponse =>
              server.stubFor(
                get(urlEqualTo(s"$functionalErrorUri"))
                  .willReturn(
                    aResponse()
                      .withStatus(errorResponse)
                  )
              )

              connector.getFunctionalErrorDescription(functionalError).futureValue mustBe FunctionalErrorWithDesc(functionalError, "")
          }
        }
      }
    }
  }
}

object ReferenceDataConnectorSpec {

  private val typeOfControl      = "44"
  private val functionalError    = "14"
  private val customsOfficeUri   = "/test-only/transit-movements-trader-reference-data/customs-office"
  private val controlTypeUri     = s"/test-only/transit-movements-trader-reference-data/control-type/$typeOfControl"
  private val functionalErrorUri = s"/test-only/transit-movements-trader-reference-data/functional-error-type/$functionalError"

  private val customsOfficeResponseJsonWithPhone: String =
    """
      | {
      |   "id":"ID1",
      |   "name":"NAME001",
      |   "phoneNumber":"004412323232345"
      | }
      |""".stripMargin

  private val customsOfficeResponseJsonWithOutPhone: String =
    """
      | {
      |   "id":"ID1",
      |   "name":"NAME001",
      |   "countryId":"GB"
      | }
      |""".stripMargin

  private val controlType: String =
    """
      | {
      |   "code":"44",
      |   "description":"Intrusive"
      | }
      |""".stripMargin

  private val functionalErrorJson: String =
    """
      | {
      |   "code":"14",
      |   "description":"Rule violation"
      | }
      |""".stripMargin
}
