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
import play.api.test.Helpers.running

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private lazy val asyncCacheApi: AsyncCacheApi = app.injector.instanceOf[AsyncCacheApi]

  private val phase5App: GuiceApplicationBuilder => GuiceApplicationBuilder = _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> false)
  private val phase6App: GuiceApplicationBuilder => GuiceApplicationBuilder = _ => guiceApplicationBuilder().configure("feature-flags.phase-6-enabled" -> true)

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

      "when phase-6 enabled" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = CustomsOffice(code, "NAME001", Some("004412323232345"), Some("test123@gmail.com"))

              connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
          }

        }
        "should throw a NoReferenceDataFoundException for an empty response " in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCustomsOffice(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCustomsOffice(code))
          }
        }
      }

      "when phase-6-disabled" - {
        val url = s"$baseUrl/lists/CustomsOffices?data.id=$code"

        val customsOfficesResponseJson: String =
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

        "should handle a 200 response for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(customsOfficesResponseJson))
              )

              val expectedResult = CustomsOffice(code, "NAME001", Some("004412323232345"), Some("test123@gmail.com"))

              connector.getCustomsOffice(code).futureValue.value mustEqual expectedResult
          }

        }
        "should throw a NoReferenceDataFoundException for an empty response " in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCustomsOffice(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCustomsOffice(code))
          }
        }
      }
    }

    "getCountry" - {

      val code = "GB"

      "when phase-6 enabled" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countriesResponseJson))
              )
              val expectedResult = Country(code, "United Kingdom")

              connector.getCountry(code).futureValue.value mustEqual expectedResult
          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCountry(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountry(code))
          }
        }
      }

      "when phase-6-disabled" - {
        val url = s"$baseUrl/lists/CountryCodesFullList?data.code=$code"

        val countriesResponseJson: String =
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

        "should handle a 200 response for countries" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(countriesResponseJson))
              )
              val expectedResult = Country(code, "United Kingdom")

              connector.getCountry(code).futureValue.value mustEqual expectedResult
          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCountry(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountry(code))
          }
        }
      }

    }

    "getCountryCodesOptOut" - {

      val code = "GB"

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(countriesOptOutResponseJson))
              )

              val expectedResult = Country("GB", "United Kingdom")

              connector.getCountryCodesOptOut(code).futureValue.value mustEqual expectedResult
          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getCountryCodesOptOut(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountryCodesOptOut(code))
          }
        }
      }

      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/CountryCodesOptOut?data.code=$code"

        val countriesResponseJson: String =
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs-reference-data/lists/CountryCodesOptOut"
             |    }
             |  },
             |  "meta": {
             |    "version": "fb16648c-ea06-431e-bbf6-483dc9ebed6e",
             |    "snapshotDate": "2023-01-01"
             |  },
             |  "id": "CountryCodesOptOut",
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

        "should handle a 200 response for countries" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(countriesResponseJson))
              )
              val expectedResult = Country(code, "United Kingdom")

              connector.getCountryCodesOptOut(code).futureValue.value mustEqual expectedResult
          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getCountryCodesOptOut(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getCountryCodesOptOut(code))
          }
        }
      }
    }

    "getQualifierOfIdentifications" - {

      val qualifier = "U"

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(qualifierOfIdentificationResponseJson))
              )

              val expectedResult = QualifierOfIdentification("U", "UN/LOCODE")

              connector.getQualifierOfIdentification(qualifier).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getQualifierOfIdentification(qualifier))
          }

        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfIdentification(qualifier))
          }
        }
      }
      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/QualifierOfTheIdentification?data.qualifier=$qualifier"

        val qualifierOfIdentificationResponseJson: String =
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

        "should handle a 200 response for identifications" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(qualifierOfIdentificationResponseJson))
              )

              val expectedResult = QualifierOfIdentification("U", "UN/LOCODE")

              connector.getQualifierOfIdentification(qualifier).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getQualifierOfIdentification(qualifier))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getQualifierOfIdentification(qualifier))
          }
        }
      }

    }

    "getIdentificationTypes" - {

      val idType = "10"

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(transportIdentifiersResponseJson))
              )
              val expectedResult = IdentificationType(idType, "IMO Ship Identification Number")

              connector.getIdentificationType(idType).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getIdentificationType(idType))
          }

        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIdentificationType(idType))
          }
        }
      }
      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/TypeOfIdentificationOfMeansOfTransport?data.type=$idType"

        val transportIdentifiersResponseJson: String =
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

        "should handle a 200 response for identification types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(transportIdentifiersResponseJson))
              )
              val expectedResult = IdentificationType(idType, "IMO Ship Identification Number")

              connector.getIdentificationType(idType).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getIdentificationType(idType))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIdentificationType(idType))
          }
        }
      }

    }

    "getNationality" - {

      val code = "AR"

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(nationalitiesResponseJson))
              )
              val expectedResult = Nationality(code, "Argentina")

              connector.getNationality(code).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getNationality(code))
          }

        }

        "should handle client and server errors for customs offices" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getNationality(code))
          }
        }
      }

      "when phase-6-disabled" - {
        val url = s"$baseUrl/lists/Nationality?data.code=$code"

        val nationalitiesResponseJson: String =
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

        "should handle a 200 response for nationalities" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(nationalitiesResponseJson))
              )
              val expectedResult = Nationality(code, "Argentina")

              connector.getNationality(code).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getNationality(code))
          }
        }

        "should handle client and server errors for customs offices" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getNationality(code))
          }
        }
      }
    }

    "getControlType" - {

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(controlTypesResponseJson))
              )
              val expectedResult = ControlType(typeOfControl, "Intrusive")

              connector.getControlType(typeOfControl).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getControlType(typeOfControl))
          }

        }

        "should handle client and server errors for control types" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getControlType(typeOfControl))
          }
        }
      }

      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/ControlType?data.code=$typeOfControl"

        val controlTypesResponseJson: String =
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

        "should handle a 200 response for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(controlTypesResponseJson))
              )
              val expectedResult = ControlType(typeOfControl, "Intrusive")

              connector.getControlType(typeOfControl).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getControlType(typeOfControl))
          }
        }

        "should handle client and server errors for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getControlType(typeOfControl))
          }
        }
      }

    }

    "getIncidentCode" - {

      "when-phase-6 enabled" - {
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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getIncidentCode(incidentCodeCode))
          }
        }

        "should handle client and server errors for incident codes" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIncidentCode(incidentCodeCode))
          }
        }
      }

      "when-phase-6-disabled" - {

        val url = s"$baseUrl/lists/IncidentCode?data.code=$incidentCodeCode"

        val incidentCodeResponseJson: String =
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

        "should handle a 200 response for incident codes" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
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

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getIncidentCode(incidentCodeCode))
          }
        }

        "should handle client and server errors for incident codes" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getIncidentCode(incidentCodeCode))
          }
        }
      }

    }

    "getRequestedDocumentType" - {

      "when phase-6 enabled" - {

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
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(requestedDocumentTypeJson))
              )
              val expectedResult = RequestedDocumentType(requestedDocumentType, "T2FL document")

              connector.getRequestedDocumentType(requestedDocumentType).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getRequestedDocumentType(requestedDocumentType))
          }
        }

        "should handle client and server errors for control types" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getRequestedDocumentType(requestedDocumentType))
          }
        }
      }

      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/RequestedDocumentType?data.code=$requestedDocumentType"

        val requestedDocumentTypeJson: String =
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

        "should handle a 200 response for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(requestedDocumentTypeJson))
              )
              val expectedResult = RequestedDocumentType(requestedDocumentType, "T2FL document")

              connector.getRequestedDocumentType(requestedDocumentType).futureValue.value mustEqual expectedResult

          }
        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getRequestedDocumentType(requestedDocumentType))
          }
        }

        "should handle client and server errors for control types" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getRequestedDocumentType(requestedDocumentType))
          }
        }
      }

    }

    "getFunctionalErrorCodesIeCA" - {

      "when phase-6 enabled" - {

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

          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(functionalErrorsResponseJson))
              )
              val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

              connector.getFunctionalErrorCodesIeCA(functionalError).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getFunctionalErrorCodesIeCA(functionalError))
          }

        }

        "should handle client and server errors for functional errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getFunctionalError(functionalError))
          }
        }
      }

      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/FunctionalErrorCodesIeCA?data.code=$functionalError"

        val functionalErrorsResponseJson: String =
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

        "should handle a 200 response for functional errors" in {

          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(functionalErrorsResponseJson))
              )
              val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

              connector.getFunctionalErrorCodesIeCA(functionalError).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getFunctionalErrorCodesIeCA(functionalError))
        }

        "should handle client and server errors for functional errors" in {
          checkErrorResponse(url, connector.getFunctionalErrorCodesIeCA(functionalError))
        }
      }

    }

    "getFunctionErrorCodesTED" - {

      "when phase-6 enabled" - {

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

          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(functionalErrorsResponseJson))
              )
              val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

              connector.getFunctionErrorCodesTED(functionalError).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getFunctionErrorCodesTED(functionalError))
          }

        }

        "should handle client and server errors for functional errors" in {
          checkErrorResponse(url, connector.getFunctionErrorCodesTED(functionalError))
        }
      }
      "when phase-6-disabled" - {

        val url = s"$baseUrl/lists/FunctionErrorCodesTED?data.code=$functionalError"

        val functionalErrorsResponseJson: String =
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

        "should handle a 200 response for functional errors" in {

          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(functionalErrorsResponseJson))
              )
              val expectedResult = FunctionalErrorWithDesc(functionalError, "Rule violation")

              connector.getFunctionErrorCodesTED(functionalError).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getFunctionalError(functionalError))
          }
        }

        "should handle client and server errors for functional errors" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getFunctionalError(functionalError))
          }
        }
      }

    }

    "getInvalidGuaranteeReason" - {
      "when phase-6 enabled" - {
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

          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.2.0+json"))
                  .willReturn(okJson(invalidGuaranteeReasonsResponseJson))
              )
              val expectedResult = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

              connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase6ResponseJson, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
          }
        }

        "should handle client and server errors for invalid guarantee reasons" in {
          running(phase6App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
          }
        }
      }

      "when phase-6-disabled" - {
        val url = s"$baseUrl/lists/InvalidGuaranteeReason?data.code=$invalidGuaranteeReasonCode"
        val invalidGuaranteeReasonsResponseJson: String =
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

        "should handle a 200 response for invalid guarantee reasons" in {

          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              server.stubFor(
                get(urlEqualTo(url))
                  .withHeader("Accept", equalTo("application/vnd.hmrc.1.0+json"))
                  .willReturn(okJson(invalidGuaranteeReasonsResponseJson))
              )
              val expectedResult = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

              connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode).futureValue.value mustEqual expectedResult

          }

        }

        "should throw a NoReferenceDataFoundException for an empty response" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkNoReferenceDataFoundResponse(url, emptyPhase5ResponseJson, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
          }
        }

        "should handle client and server errors for invalid guarantee reasons" in {
          running(phase5App) {
            app =>
              val connector = app.injector.instanceOf[ReferenceDataConnector]
              checkErrorResponse(url, connector.getInvalidGuaranteeReason(invalidGuaranteeReasonCode))
          }
        }
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

  private val emptyPhase5ResponseJson: String =
    """
      |{
      |  "data": []
      |}
      |""".stripMargin

  private val emptyPhase6ResponseJson: String =
    """
      |[]
      |""".stripMargin
}
