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
import connectors.ReferenceDataConnector
import models.referenceData.*
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.{never, verify, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase {

  "ReferenceDataService" - {

    "getCustomsOffice" - {

      val customsOfficeId = "GB00001"
      val customsOffice1  = CustomsOffice(customsOfficeId, "CO1", None, None)

      "should return a valid customs office" in new SetUp {
        when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Right(customsOffice1)))

        service.getCustomsOffice(customsOfficeId).futureValue mustEqual customsOffice1

        verify(mockConnector).getCustomsOffice(any())(any(), any())
      }
    }

    "getCountry" - {

      val countryCode1 = "GB"
      val country1     = Country(countryCode1, "United Kingdom")

      "should return a valid country" in new SetUp {
        when(mockConnector.getCountry(any())(any(), any())).thenReturn(Future.successful(Right(country1)))

        service.getCountry(countryCode1).futureValue mustEqual country1

        verify(mockConnector).getCountry(any())(any(), any())
      }
    }

    "getQualifierOfIdentification" - {

      val identificationCode = "U"
      val identification     = QualifierOfIdentification("U", "UN/LOCODE")

      "should return the qualifier of identification" in new SetUp {
        when(mockConnector.getQualifierOfIdentification(any())(any(), any())).thenReturn(Future.successful(Right(identification)))

        service.getQualifierOfIdentification(identificationCode).futureValue mustEqual identification

        verify(mockConnector).getQualifierOfIdentification(any())(any(), any())
      }
    }

    "getIdentificationType" - {

      val identificationTypeCode = "10"
      val identificationType     = IdentificationType(identificationTypeCode, "IMO Ship Identification Number")

      "should return identification type" in new SetUp {
        when(mockConnector.getIdentificationType(any())(any(), any())).thenReturn(Future.successful(Right(identificationType)))

        service.getIdentificationType(identificationTypeCode).futureValue mustEqual identificationType

        verify(mockConnector).getIdentificationType(any())(any(), any())
      }
    }

    "getNationalities" - {
      val nationalityCode = "GB"
      val nationality     = Nationality(nationalityCode, "British")

      "should return nationalities" in new SetUp {
        when(mockConnector.getNationality(any())(any(), any())).thenReturn(Future.successful(Right(nationality)))

        service.getNationality(nationalityCode).futureValue mustEqual nationality

        verify(mockConnector).getNationality(any())(any(), any())
      }
    }

    "getControlType" - {

      val controlTypeCode = "1"
      val controlType     = ControlType(controlTypeCode, "CT1")

      "should return a control type" in new SetUp {
        when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(Right(controlType)))

        service.getControlType(controlTypeCode).futureValue mustEqual controlType

        verify(mockConnector).getControlType(any())(any(), any())
      }
    }

    "getIncidentCode" - {

      val incidentCodeCode = "1"

      val incidentCode =
        IncidentCode(
          incidentCodeCode,
          "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of " +
            "UCC/IA Regulation due to circumstances beyond his control."
        )

      "should return a incident code" in new SetUp {

        when(mockConnector.getIncidentCode(any())(any(), any()))
          .thenReturn(Future.successful(Right(incidentCode)))

        service.getIncidentCode(incidentCodeCode).futureValue mustEqual incidentCode

        verify(mockConnector).getIncidentCode(any())(any(), any())
      }
    }

    "getRequestedDocumentType" - {

      val requestedDocumentTypeCode = "C620"
      val requestedDocumentType     = RequestedDocumentType(requestedDocumentTypeCode, "T2FL document")

      "should return a requested document type" in new SetUp {

        when(mockConnector.getRequestedDocumentType(any())(any(), any()))
          .thenReturn(Future.successful(Right(requestedDocumentType)))

        service.getRequestedDocumentType(requestedDocumentTypeCode).futureValue mustEqual requestedDocumentType

        verify(mockConnector).getRequestedDocumentType(any())(any(), any())
      }
    }

    "getFunctionalError" - {

      val functionalErrorCode = "1"
      val functionalError     = FunctionalErrorWithDesc(functionalErrorCode, "FE1")

      "should return a functional error" in new SetUp {
        when(mockConnector.getFunctionalErrorCodesIeCA(any())(any(), any())).thenReturn(Future.successful(Right(functionalError)))

        service.getFunctionalError(functionalErrorCode).futureValue mustEqual functionalError

        verify(mockConnector).getFunctionalErrorCodesIeCA(any())(any(), any())
      }

    }

    "getFunctionalErrorForSender" - {

      val functionalErrorCode = "1"
      val functionalError     = FunctionalErrorWithDesc(functionalErrorCode, "FE1")

      "should return FunctionalErrorCodesIeCA (CL180) if messageSender is set in CL167 (countryCodesOptOut)" in new SetUp {

        val messageSender = "1745GB"
        val countryCode   = "GB"

        when(mockConnector.getCountryCodesOptOut(eqTo(countryCode))(any(), any()))
          .thenReturn(Future.successful(Right(Country(countryCode, "United Kingdom"))))

        when(mockConnector.getFunctionalErrorCodesIeCA(eqTo(functionalErrorCode))(any(), any()))
          .thenReturn(Future.successful(Right(functionalError)))

        val result: FunctionalErrorWithDesc = service.getFunctionalErrorForSender(functionalErrorCode, messageSender).futureValue

        result mustBe functionalError

        verify(mockConnector).getCountryCodesOptOut(eqTo(countryCode))(any(), any())
        verify(mockConnector).getFunctionalErrorCodesIeCA(eqTo(functionalErrorCode))(any(), any())
        verify(mockConnector, never()).getFunctionErrorCodesTED(any[String])(any(), any())
      }

      "should return FunctionErrorCodesTED (CL437) if messageSender is NOT set in CL167 (countryCodesOptOut)" in new SetUp {

        val messageSender = "1679FR"
        val countryCode   = "FR"

        when(mockConnector.getCountryCodesOptOut(eqTo(countryCode))(any(), any()))
          .thenReturn(Future.successful(Left(new Exception("not_found"))))

        when(mockConnector.getFunctionErrorCodesTED(eqTo(functionalErrorCode))(any(), any()))
          .thenReturn(Future.successful(Right(functionalError)))

        val result: FunctionalErrorWithDesc = service.getFunctionalErrorForSender(functionalErrorCode, messageSender).futureValue

        result mustBe functionalError

        verify(mockConnector).getCountryCodesOptOut(eqTo(countryCode))(any(), any())
        verify(mockConnector, never()).getFunctionalErrorCodesIeCA(any[String])(any(), any())
        verify(mockConnector).getFunctionErrorCodesTED(eqTo(functionalErrorCode))(any(), any())
      }

    }

    "getInvalidGuaranteeReason" - {

      val invalidGuaranteeReasonCode = "G02"
      val invalidGuaranteeReason     = InvalidGuaranteeReason(invalidGuaranteeReasonCode, "Guarantee exists, but not valid")

      "should return a invalid guarantee reason" in new SetUp {
        when(mockConnector.getInvalidGuaranteeReason(any())(any(), any())).thenReturn(Future.successful(Right(invalidGuaranteeReason)))

        service.getInvalidGuaranteeReason(invalidGuaranteeReasonCode).futureValue mustEqual invalidGuaranteeReason

        verify(mockConnector).getInvalidGuaranteeReason(any())(any(), any())
      }
    }
  }

  trait SetUp {
    val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
    val service                               = ReferenceDataService(mockConnector)
  }

}
