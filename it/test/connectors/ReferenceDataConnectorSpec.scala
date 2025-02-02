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

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec.*
import itbase.{ItSpecBase, WireMockServerHandler}
import models.referenceData.*
import models.{Country, IdentificationType, IncidentCode, Nationality, QualifierOfIdentification}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private lazy val asyncCacheApi: AsyncCacheApi      = app.injector.instanceOf[AsyncCacheApi]
  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.customs-reference-data.port" -> server.port())

  override def beforeEach(): Unit = {
    super.beforeEach()
    asyncCacheApi.removeAll().futureValue
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[?]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(emptyResponseJson))
    )

    whenReady[Throwable, Assertion](result.failed) {
      _ `mustBe` a[NoReferenceDataFoundException]
    }
  }

  private def checkErrorResponse(url: String, result: => Future[?]): Assertion = {
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
          _ `mustBe` an[Exception]
        }
    }
  }

  private val queryParams = "foo" -> "bar"

  "Reference Data" - {

    "getCustomsOffice" - {

      val url = s"$baseUrl/lists/CustomsOffices?foo=bar"

      "should handle a 200 response for customs offices" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = NonEmptySet.of(CustomsOffice(code, "NAME001", Some("004412323232345")))

        connector.getCustomsOffices(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOffices(queryParams))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCustomsOffices(queryParams))
      }
    }

    "getCountries" - {

      val url = s"$baseUrl/lists/CountryCodesFullList?foo=bar"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(countriesResponseJson))
        )

        val expectedResult = NonEmptySet.of(Country("GB", "United Kingdom"), Country("AD", "Andorra"))

        connector.getCountries(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountries(queryParams))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCountries(queryParams))
      }
    }

    "getQualifierOfIdentifications" - {

      val url = s"$baseUrl/lists/QualifierOfTheIdentification?foo=bar"

      "should handle a 200 response for identifications" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(qualifierOfIdentificationResponseJson))
        )

        val expectedResult = NonEmptySet.of(QualifierOfIdentification("U", "UN/LOCODE"), QualifierOfIdentification("Z", "Address"))

        connector.getQualifierOfIdentifications(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getQualifierOfIdentifications(queryParams))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getQualifierOfIdentifications(queryParams))
      }
    }

    "getIdentificationTypes" - {

      val url = s"$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?foo=bar"

      "should handle a 200 response for identification types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(transportIdentifiersResponseJson))
        )

        val expectedResult = NonEmptySet.of(
          IdentificationType("10", "IMO Ship Identification Number"),
          IdentificationType("11", "Name of the sea-going vessel")
        )

        connector.getIdentificationTypes(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIdentificationTypes(queryParams))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getIdentificationTypes(queryParams))
      }
    }

    "getNationalities" - {

      val url = s"$baseUrl/lists/Nationality?foo=bar"

      "should handle a 200 response for nationalities" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(nationalitiesResponseJson))
        )

        val expectedResult = NonEmptySet.of(Nationality("AR", "Argentina"), Nationality("AU", "Australia"))

        connector.getNationalities(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getNationalities(queryParams))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getNationalities(queryParams))
      }
    }

    "getControlType" - {

      val url = s"$baseUrl/lists/ControlType?foo=bar"

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(controlTypesResponseJson))
        )

        val expectedResult = ControlType(typeOfControl, "Intrusive")

        connector.getControlType(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getControlType(queryParams))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getControlType(queryParams))
      }
    }

    "getIncidentCode" - {

      val url = s"$baseUrl/lists/IncidentCode?foo=bar"

      "should handle a 200 response for incident codes" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(incidentCodeResponseJson))
        )

        val expectedResult = IncidentCode(
          incidentCodeCode,
          "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
        )

        connector.getIncidentCode(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIncidentCode(queryParams))
      }

      "should handle client and server errors for incident codes" in {
        checkErrorResponse(url, connector.getIncidentCode(queryParams))
      }
    }

    "getRequestedDocumentType" - {

      val url = s"$baseUrl/lists/RequestedDocumentType?foo=bar"

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(requestedDocumentTypeJson))
        )

        val expectedResult = RequestedDocumentType("C620", "T2FL document")

        connector.getRequestedDocumentType(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getRequestedDocumentType(queryParams))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getRequestedDocumentType(queryParams))
      }
    }

    "getFunctionalError" - {

      val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA?foo=bar"

      "should handle a 200 response for functional errors" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(functionalErrorsResponseJson))
        )

        val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

        connector.getFunctionalError(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getFunctionalError(queryParams))
      }

      "should handle client and server errors for functional errors" in {
        checkErrorResponse(url, connector.getFunctionalError(queryParams))
      }
    }

    "getInvalidGuaranteeReason" - {

      val url = s"$baseUrl/lists/InvalidGuaranteeReason?foo=bar"

      "should handle a 200 response for invalid guarantee reasons" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(invalidGuaranteeReasonsResponseJson))
        )

        val expectedResult = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

        connector.getInvalidGuaranteeReason(queryParams).futureValue `mustBe` expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getInvalidGuaranteeReason(queryParams))
      }

      "should handle client and server errors for invalid guarantee reasons" in {
        checkErrorResponse(url, connector.getInvalidGuaranteeReason(queryParams))
      }
    }
  }

}

object ReferenceDataConnectorSpec {

  private val code                       = "GB00001"
  private val typeOfControl              = "44"
  private val incidentCodeCode           = "1"
  private val requestedDocumentType      = "C620"
  private val functionalError            = "14"
  private val invalidGuaranteeReasonCode = "G02"

  private val baseUrl = "/customs-reference-data/test-only"

  private val customsOfficesResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "id": "$code",
       |      "name": "NAME001",
       |      "languageCode": "EN",
       |      "phoneNumber": "004412323232345"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val qualifierOfIdentificationResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |      "qualifier": "U",
      |      "description": "UN/LOCODE"
      |    },
      |    {
      |      "qualifier": "Z",
      |      "description": "Address"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val countriesResponseJson: String =
    s"""
       |{
       |  "_links": {
       |    "self": {
       |      "href": "/customs-reference-data/lists/CountryCodesFullList"
       |    }
       |  },
       |  "meta": {
       |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
       |    "snapshotDate": "2023-01-01"
       |  },
       |  "id": "CountryCodesFullList",
       |  "data": [
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "GB",
       |      "state": "valid",
       |      "description": "United Kingdom"
       |    },
       |    {
       |      "activeFrom": "2023-01-23",
       |      "code": "AD",
       |      "state": "valid",
       |      "description": "Andorra"
       |    }
       |  ]
       |}
       |""".stripMargin

  private val transportIdentifiersResponseJson: String =
    """
      |{
      |  "data": [
      |    {
      |     "type": "10",
      |     "description": "IMO Ship Identification Number"
      |    },
      |    {
      |     "type": "11",
      |     "description": "Name of the sea-going vessel"
      |    }
      |  ]
      |}
      |""".stripMargin

  private val nationalitiesResponseJson: String =
    """
      |{
      |  "_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/Nationality"
      |    }
      |  },
      |  "meta": {
      |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "Nationality",
      |  "data": [
      |    {
      |      "code":"AR",
      |      "description":"Argentina"
      |    },
      |    {
      |      "code":"AU",
      |      "description":"Australia"
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

  private val incidentCodeResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "code": "$incidentCodeCode",
       |      "description": "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
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

  private val invalidGuaranteeReasonsResponseJson: String =
    s"""
       |{
       |  "data": [
       |    {
       |      "code": "$invalidGuaranteeReasonCode",
       |      "description": "Guarantee exists, but not valid"
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
