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

import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import connectors.ReferenceDataConnectorSpec.*
import itbase.{ItSpecBase, WireMockServerHandler}
import models.referenceData.*
import models.{Country, IdentificationType, IncidentCode, Nationality, QualifierOfIdentification}
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.cache.AsyncCacheApi
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

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

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
        .willReturn(okJson(emptyResponseJson))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }

  "Reference Data" - {

    "getCustomsOffice" - {

      val url = s"$baseUrl/lists/CustomsOffices?data.id=$code"

      "should handle a 200 response for customs offices" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = CustomsOffice(code, "NAME001", Some("004412323232345"), Some("test123@gmail.com"))

        connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCustomsOffice(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCustomsOffice(code))
      }
    }

    "getCountry" - {

      val code = "GB"

      val url = s"$baseUrl/lists/CountryCodesFullList?data.code=$code"

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(countriesResponseJson))
        )

        val expectedResult = Country(code, "United Kingdom")

        connector.getCountry(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getCountry(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCountry(code))
      }
    }

    "getQualifierOfIdentifications" - {

      val qualifier = "U"

      val url = s"$baseUrl/lists/QualifierOfTheIdentification?data.qualifier=$qualifier"

      "should handle a 200 response for identifications" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(qualifierOfIdentificationResponseJson))
        )

        val expectedResult = QualifierOfIdentification("U", "UN/LOCODE")

        connector.getQualifierOfIdentification(qualifier).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getQualifierOfIdentification(qualifier))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getQualifierOfIdentification(qualifier))
      }
    }

    "getIdentificationTypes" - {

      val idType = "10"

      val url = s"$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?data.type=$idType"

      "should handle a 200 response for identification types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(transportIdentifiersResponseJson))
        )

        val expectedResult = IdentificationType(idType, "IMO Ship Identification Number")

        connector.getIdentificationType(idType).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIdentificationType(idType))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getIdentificationType(idType))
      }
    }

    "getNationality" - {

      val code = "AR"

      val url = s"$baseUrl/lists/Nationality?data.code=$code"

      "should handle a 200 response for nationalities" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(nationalitiesResponseJson))
        )

        val expectedResult = Nationality(code, "Argentina")

        connector.getNationality(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getNationality(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getNationality(code))
      }
    }

    "getControlType" - {

      val url = s"$baseUrl/lists/ControlType?data.code=$typeOfControl"

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(controlTypesResponseJson))
        )

        val expectedResult = ControlType(typeOfControl, "Intrusive")

        connector.getControlType(typeOfControl).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getControlType(typeOfControl))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getControlType(typeOfControl))
      }
    }

    "getIncidentCode" - {

      val url = s"$baseUrl/lists/IncidentCode?data.code=$incidentCodeCode"

      "should handle a 200 response for incident codes" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(incidentCodeResponseJson))
        )

        val expectedResult = IncidentCode(
          incidentCodeCode,
          "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
        )

        connector.getIncidentCode(incidentCodeCode).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getIncidentCode(incidentCodeCode))
      }

      "should handle client and server errors for incident codes" in {
        checkErrorResponse(url, connector.getIncidentCode(incidentCodeCode))
      }
    }

    "getRequestedDocumentType" - {

      val url = s"$baseUrl/lists/RequestedDocumentType?data.code=$requestedDocumentType"

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(requestedDocumentTypeJson))
        )

        val expectedResult = RequestedDocumentType(requestedDocumentType, "T2FL document")

        connector.getRequestedDocumentType(requestedDocumentType).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getRequestedDocumentType(requestedDocumentType))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getRequestedDocumentType(requestedDocumentType))
      }
    }

    "getFunctionalError" - {

      val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA?data.code=$functionalError"

      "should handle a 200 response for functional errors" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(functionalErrorsResponseJson))
        )

        val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

        connector.getFunctionalError(functionalError).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getFunctionalError(functionalError))
      }

      "should handle client and server errors for functional errors" in {
        checkErrorResponse(url, connector.getFunctionalError(functionalError))
      }
    }

    "getInvalidGuaranteeReason" - {

      val url = s"$baseUrl/lists/InvalidGuaranteeReason?data.code=$invalidGuaranteeReasonCode"

      "should handle a 200 response for invalid guarantee reasons" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(invalidGuaranteeReasonsResponseJson))
        )

        val expectedResult = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

        connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
      }

      "should handle client and server errors for invalid guarantee reasons" in {
        checkErrorResponse(url, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
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
       |      "phoneNumber": "004412323232345",
       |      "eMailAddress": "test123@gmail.com"
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
