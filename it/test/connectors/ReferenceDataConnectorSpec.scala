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

  private def checkNoReferenceDataFoundResponse(url: String, json: String, result: => Future[Either[Exception, ?]]): Assertion = {
    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
    )

    result.futureValue.left.value mustBe a[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
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

        result.futureValue.left.value mustBe an[Exception]
    }
  }

  "Reference Data" - {

    "getCustomsOffice" - {
      val url = s"$baseUrl/lists/CustomsOffices?referenceNumbers=$code"

      val customsOfficesResponseJson: String =
        s"""
             |[
             |  {
             |    "referenceNumber": "$code",
             |    "customsOfficeLsd" : {
             |      "customsOfficeUsualName" : "NAME001",
             |      "languageCode" : "EN"
             |    },
             |    "phoneNumber": "004412323232345",
             |    "emailAddress": "test123@gmail.com"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for customs offices" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(customsOfficesResponseJson))
        )

        val expectedResult = CustomsOffice(code, "NAME001", Some("004412323232345"), Some("test123@gmail.com"))

        connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
      }
      "should throw a NoReferenceDataFoundException for an empty response " in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getCustomsOffice(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCustomsOffice(code))
      }
    }

    "getCountry" - {

      val code = "GB"

      val url = s"$baseUrl/lists/CountryCodesFullList?keys=$code"

      val countriesResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "GB",
             |    "value": "United Kingdom",
             |    "properties": {
             |      "state": "valid"
             |    }
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(countriesResponseJson))
        )
        val expectedResult = Country(code, "United Kingdom")

        connector.getCountry(code).futureValue.value mustEqual expectedResult

      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getCountry(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCountry(code))
      }
    }

    "getCountryCodesOptOut" - {

      val code = "GB"

      val url = s"$baseUrl/lists/CountryCodesOptOut?keys=GB"

      val countriesOptOutResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "GB",
             |    "value": "United Kingdom",
             |    "properties": {
             |      "state": "valid"
             |     }
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for opt-out countries" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(countriesOptOutResponseJson))
        )

        val expectedResult = Country("GB", "United Kingdom")

        connector.getCountryCodesOptOut(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getCountryCodesOptOut(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getCountryCodesOptOut(code))
      }
    }

    "getQualifierOfIdentifications" - {

      val qualifier = "U"

      val url = s"$baseUrl/lists/QualifierOfTheIdentification?keys=$qualifier"

      val qualifierOfIdentificationResponseJson: String =
        """
            |[
            |  {
            |    "key": "U",
            |    "value": "UN/LOCODE"
            |  }
            |]
            |""".stripMargin

      "should handle a 200 response for identifications" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(qualifierOfIdentificationResponseJson))
        )

        val expectedResult = QualifierOfIdentification("U", "UN/LOCODE")

        connector.getQualifierOfIdentification(qualifier).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getQualifierOfIdentification(qualifier))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getQualifierOfIdentification(qualifier))
      }
    }

    "getIdentificationTypes" - {

      val idType = "10"

      val url = s"$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?keys=$idType"

      val transportIdentifiersResponseJson: String =
        """
            |[
            |  {
            |   "key": "10",
            |   "value": "IMO Ship Identification Number"
            |  },
            |  {
            |   "key": "11",
            |   "value": "Name of the sea-going vessel"
            |  }
            |]
            |""".stripMargin

      "should handle a 200 response for identification types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(transportIdentifiersResponseJson))
        )
        val expectedResult = IdentificationType(idType, "IMO Ship Identification Number")

        connector.getIdentificationType(idType).futureValue.value mustEqual expectedResult

      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        val connector = app.injector.instanceOf[ReferenceDataConnector]
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getIdentificationType(idType))
      }

      "should handle client and server errors for customs offices" in {
        val connector = app.injector.instanceOf[ReferenceDataConnector]
        checkErrorResponse(url, connector.getIdentificationType(idType))
      }
    }

    "getNationality" - {

      val code = "AR"

      val url = s"$baseUrl/lists/Nationality?keys=$code"
      val nationalitiesResponseJson: String =
        """
            |[
            |  {
            |    "key":"AR",
            |    "value":"Argentina"
            |  }
            |]
            |""".stripMargin

      "should handle a 200 response for nationalities" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(nationalitiesResponseJson))
        )
        val expectedResult = Nationality(code, "Argentina")

        connector.getNationality(code).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getNationality(code))
      }

      "should handle client and server errors for customs offices" in {
        checkErrorResponse(url, connector.getNationality(code))
      }
    }

    "getControlType" - {

      val url = s"$baseUrl/lists/ControlType?keys=$typeOfControl"

      val controlTypesResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "$typeOfControl",
             |    "value": "Intrusive"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(controlTypesResponseJson))
        )
        val expectedResult = ControlType(typeOfControl, "Intrusive")

        connector.getControlType(typeOfControl).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getControlType(typeOfControl))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getControlType(typeOfControl))
      }
    }

    "getIncidentCode" - {

      val url = s"$baseUrl/lists/IncidentCode?keys=$incidentCodeCode"

      val incidentCodeResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "$incidentCodeCode",
             |    "value": "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for incident codes" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(incidentCodeResponseJson))
        )
        val expectedResult = IncidentCode(
          incidentCodeCode,
          "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
        )

        connector.getIncidentCode(incidentCodeCode).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getIncidentCode(incidentCodeCode))
      }

      "should handle client and server errors for incident codes" in {
        checkErrorResponse(url, connector.getIncidentCode(incidentCodeCode))
      }
    }

    "getRequestedDocumentType" - {

      val url = s"$baseUrl/lists/RequestedDocumentType?keys=$requestedDocumentType"

      val requestedDocumentTypeJson: String =
        s"""
             |[
             |  {
             |    "key": "$requestedDocumentType",
             |    "value": "T2FL document"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for control types" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(requestedDocumentTypeJson))
        )
        val expectedResult = RequestedDocumentType(requestedDocumentType, "T2FL document")

        connector.getRequestedDocumentType(requestedDocumentType).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getRequestedDocumentType(requestedDocumentType))
      }

      "should handle client and server errors for control types" in {
        checkErrorResponse(url, connector.getRequestedDocumentType(requestedDocumentType))
      }
    }

    "getFunctionalErrorCodesIeCA" - {

      val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA?keys=$functionalError"

      val functionalErrorsResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "$functionalError",
             |    "value": "Rule violation"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for functional errors" in {

        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(functionalErrorsResponseJson))
        )
        val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

        connector.getFunctionalErrorCodesIeCA(functionalError).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getFunctionalErrorCodesIeCA(functionalError))
      }

      "should handle client and server errors for functional errors" in {
        checkErrorResponse(url, connector.getFunctionalErrorCodesIeCA(functionalError))
      }
    }

    "getFunctionErrorCodesTED" - {

      val url = s"$baseUrl/lists/FunctionErrorCodesTED?keys=$functionalError"

      val functionalErrorsResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "$functionalError",
             |    "value": "Rule violation"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for functional errors" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(functionalErrorsResponseJson))
        )
        val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

        connector.getFunctionErrorCodesTED(functionalError).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getFunctionErrorCodesTED(functionalError))
      }

      "should handle client and server errors for functional errors" in {
        checkErrorResponse(url, connector.getFunctionErrorCodesTED(functionalError))
      }
    }

    "getInvalidGuaranteeReason" - {
      val url = s"$baseUrl/lists/InvalidGuaranteeReason?keys=$invalidGuaranteeReasonCode"

      val invalidGuaranteeReasonsResponseJson: String =
        s"""
             |[
             |  {
             |    "key": "$invalidGuaranteeReasonCode",
             |    "value": "Guarantee exists, but not valid"
             |  }
             |]
             |""".stripMargin

      "should handle a 200 response for invalid guarantee reasons" in {
        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
            .willReturn(okJson(invalidGuaranteeReasonsResponseJson))
        )
        val expectedResult = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

        connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode).futureValue.value mustEqual expectedResult
      }

      "should throw a NoReferenceDataFoundException for an empty response" in {
        checkNoReferenceDataFoundResponse(url, emptyResponseJson, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
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

  private val emptyResponseJson: String =
    """
      |[]
      |""".stripMargin

}
