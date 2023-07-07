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
import connectors.CustomsReferenceDataConnector
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsReferenceDataServiceSpec extends AnyFreeSpec with ScalaFutures with Matchers with MockitoSugar with SpecBase {

  private val mockConnector: CustomsReferenceDataConnector = mock[CustomsReferenceDataConnector]

  private val customsOfficeId = "GB00001"
  private val customsOffice   = CustomsOffice(customsOfficeId, "NAME001", None)

  private val controlTypeCode           = "44"
  private val controlTypeForValidCode   = ControlType(controlTypeCode, "Intrusive")
  private val controlTypeForInvalidCode = ControlType(controlTypeCode, "")

  private val functionalErrorCode           = "14"
  private val functionalErrorForValidCode   = FunctionalErrorWithDesc(functionalErrorCode, "Rule violation")
  private val functionalErrorForInvalidCode = FunctionalErrorWithDesc(functionalErrorCode, "")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "CustomsReferenceDataService" - {

    "getCustomsOfficeByCode" - {
      "should return some customs office" - {
        "when the customs office is found" in {
          when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getCustomsOfficeByCode(customsOfficeId).futureValue.value mustBe customsOffice

          verify(mockConnector).getCustomsOffice(eqTo(customsOfficeId))(any(), any())
        }
      }

      "should return None" - {
        "when the customs office can't be found" in {
          when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(None))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getCustomsOfficeByCode(customsOfficeId).futureValue mustBe None

          verify(mockConnector).getCustomsOffice(eqTo(customsOfficeId))(any(), any())
        }
      }
    }

    "getControlType" - {
      "should return a control type" - {
        "when the control type is found" in {
          when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(Some(controlTypeForValidCode)))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe controlTypeForValidCode

          verify(mockConnector).getControlType(eqTo(controlTypeCode))(any(), any())
        }
      }

      "should return default" - {
        "when the control type can't be found" in {
          when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(None))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe controlTypeForInvalidCode

          verify(mockConnector).getControlType(eqTo(controlTypeCode))(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe controlTypeForInvalidCode

          verify(mockConnector).getControlType(eqTo(controlTypeCode))(any(), any())
        }
      }
    }

    "getFunctionalError" - {
      "should return a functional error" - {
        "when the functional error is found" in {
          when(mockConnector.getFunctionalErrorDescription(any())(any(), any())).thenReturn(Future.successful(Some(functionalErrorForValidCode)))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrorType(functionalErrorCode).futureValue mustBe functionalErrorForValidCode

          verify(mockConnector).getFunctionalErrorDescription(eqTo(functionalErrorCode))(any(), any())
        }
      }

      "should return default" - {
        "when the functional error can't be found" in {
          when(mockConnector.getFunctionalErrorDescription(any())(any(), any())).thenReturn(Future.successful(None))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrorType(functionalErrorCode).futureValue mustBe functionalErrorForInvalidCode

          verify(mockConnector).getFunctionalErrorDescription(eqTo(functionalErrorCode))(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getFunctionalErrorDescription(any())(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new CustomsReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrorType(functionalErrorCode).futureValue mustBe functionalErrorForInvalidCode

          verify(mockConnector).getFunctionalErrorDescription(eqTo(functionalErrorCode))(any(), any())
        }
      }
    }
  }
}
