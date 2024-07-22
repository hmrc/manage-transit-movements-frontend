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

package services

import base.SpecBase
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, RequestedDocumentType}
import models.{Country, IdentificationType, IncidentCode, Nationality}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends AnyFreeSpec with ScalaFutures with Matchers with MockitoSugar with SpecBase {

  private val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  private val customsOfficeId = "GB00001"
  private val customsOffice1  = CustomsOffice(customsOfficeId, "CO1", None)
  private val customsOffice2  = CustomsOffice("GB00002", "CO2", None)
  private val customsOffices  = NonEmptySet.of(customsOffice1, customsOffice2)

  private val countryCode1 = "GB"
  private val country1     = Country(countryCode1, "United Kingdom")
  private val countries    = NonEmptySet.of(country1)

  private val identificationTypeCode = "10"
  private val identificationType     = IdentificationType(identificationTypeCode, "IMO Ship Identification Number")
  private val identificationTypes    = NonEmptySet.of(identificationType)

  private val nationalityCode = "GB"
  private val nationality     = Nationality(nationalityCode, "British")
  private val nationalities   = NonEmptySet.of(nationality)

  private val controlTypeCode = "1"
  private val controlType1    = ControlType(controlTypeCode, "CT1")
  private val controlType2    = ControlType("2", "CT2")
  private val controlTypes    = NonEmptySet.of(controlType1, controlType2)

  private val requestedDocumentTypeCode = "C620"
  private val requestedDocumentType1    = RequestedDocumentType(requestedDocumentTypeCode, "T2FL document")

  private val incidentCodeCode = "1"

  private val incidentCode =
    IncidentCode(
      incidentCodeCode,
      "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
    )

  private val functionalErrorCode = "1"
  private val functionalError1    = FunctionalErrorWithDesc(functionalErrorCode, "FE1")
  private val functionalError2    = FunctionalErrorWithDesc("2", "FE2")
  private val functionalErrors    = NonEmptySet.of(functionalError1, functionalError2)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "ReferenceDataService" - {

    "getCustomsOffice" - {

      val expectedQueryParams = Seq("data.id" -> customsOfficeId)

      "should return customs office" in {
        when(mockConnector.getCustomsOffices(eqTo(expectedQueryParams): _*)(any(), any())).thenReturn(Future.successful(customsOffices))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCustomsOffice(customsOfficeId).futureValue mustBe Right(customsOffice1)

        verify(mockConnector).getCustomsOffices(eqTo(expectedQueryParams): _*)(any(), any())
      }

      "should return Left" - {
        "when the connector call returns no data" in {
          when(mockConnector.getCustomsOffices(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCustomsOffice(customsOfficeId).futureValue mustBe Left(customsOfficeId)

          verify(mockConnector).getCustomsOffices(eqTo(expectedQueryParams): _*)(any(), any())
        }
      }
    }

    "getCountry" - {

      val expectedQueryParams = Seq("data.code" -> countryCode1)

      "should return countries" in {
        when(mockConnector.getCountries(eqTo(expectedQueryParams): _*)(any(), any())).thenReturn(Future.successful(countries))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCountry(countryCode1).futureValue mustBe Right(country1)

        verify(mockConnector).getCountries(eqTo(expectedQueryParams): _*)(any(), any())
      }

      "should return Left" - {
        "when the connector call returns no data" in {
          when(mockConnector.getCountries(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCountry(countryCode1).futureValue mustBe Left(countryCode1)

          verify(mockConnector).getCountries(eqTo(expectedQueryParams): _*)(any(), any())
        }
      }
    }

    "getIdentificationType" - {

      val expectedQueryParams = Seq("data.type" -> identificationTypeCode)

      "should return identification type" in {
        when(mockConnector.getIdentificationTypes(eqTo(expectedQueryParams): _*)(any(), any())).thenReturn(Future.successful(identificationTypes))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getIdentificationType(identificationTypeCode).futureValue mustBe Right(identificationType)

        verify(mockConnector).getIdentificationTypes(eqTo(expectedQueryParams): _*)(any(), any())
      }

      "should return Left" - {
        "when the connector call returns no data" in {
          when(mockConnector.getIdentificationTypes(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getIdentificationType(identificationTypeCode).futureValue mustBe Left(identificationTypeCode)

          verify(mockConnector).getIdentificationTypes(eqTo(expectedQueryParams): _*)(any(), any())
        }
      }
    }

    "getNationalities" - {

      val expectedQueryParams = Seq("data.code" -> nationalityCode)

      "should return nationalities" in {
        when(mockConnector.getNationalities(eqTo(expectedQueryParams): _*)(any(), any())).thenReturn(Future.successful(nationalities))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getNationality(nationalityCode).futureValue mustBe Right(nationality)

        verify(mockConnector).getNationalities(eqTo(expectedQueryParams): _*)(any(), any())
      }

      "should return Left" - {
        "when the connector call returns no data" in {
          when(mockConnector.getNationalities(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getNationality(nationalityCode).futureValue mustBe Left(nationalityCode)

          verify(mockConnector).getNationalities(eqTo(expectedQueryParams): _*)(any(), any())
        }
      }
    }

    "getControlType" - {

      val expectedQueryParams = Seq("data.code" -> controlTypeCode)

      "should return a control type" in {
        when(mockConnector.getControlTypes(any())(any(), any())).thenReturn(Future.successful(controlTypes))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getControlType(controlTypeCode).futureValue mustBe controlType1

        verify(mockConnector).getControlTypes(eqTo(expectedQueryParams): _*)(any(), any())
      }
    }

    "getIncidentCode" - {

      val expectedQueryParams = Seq("data.code" -> incidentCodeCode)

      "should return a incident code" in {

        when(mockConnector.getIncidentCodes(any())(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(incidentCode)))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getIncidentCode(incidentCodeCode).futureValue mustBe incidentCode

        verify(mockConnector).getIncidentCodes(eqTo(expectedQueryParams): _*)(any(), any())
      }
    }

    "getRequestedDocumentType" - {

      val expectedQueryParams = Seq("data.code" -> requestedDocumentTypeCode)

      "should return a requested document type" in {

        when(mockConnector.getRequestedDocumentTypes(any())(any(), any()))
          .thenReturn(Future.successful(NonEmptySet.of(requestedDocumentType1)))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getRequestedDocumentType(requestedDocumentTypeCode).futureValue mustBe requestedDocumentType1

        verify(mockConnector).getRequestedDocumentTypes(eqTo(expectedQueryParams): _*)(any(), any())
      }
    }

    "getFunctionalError" - {

      val expectedQueryParams = Seq("data.code" -> functionalErrorCode)

      "should return a functional error" in {
        when(mockConnector.getFunctionalErrors(any())(any(), any())).thenReturn(Future.successful(functionalErrors))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getFunctionalError(functionalErrorCode).futureValue mustBe functionalError1

        verify(mockConnector).getFunctionalErrors(eqTo(expectedQueryParams): _*)(any(), any())
      }

    }
  }
}
