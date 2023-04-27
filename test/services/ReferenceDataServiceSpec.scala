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
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataServiceSpec extends AnyFreeSpec with ScalaFutures with Matchers with MockitoSugar with SpecBase {

  private val mockConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  private val customsOffice                 = CustomsOffice("ID1", "NAME001", None)
  private val controlTypeForValidCode       = ControlType("44", "Intrusive")
  private val functionalErrorForValidCode   = FunctionalErrorWithDesc("14", "Rule violation")
  private val controlTypeForInvalidCode     = ControlType("44", "Intrusive")
  private val functionalErrorForInValidCode = FunctionalErrorWithDesc("999", "")

  override def beforeEach(): Unit =
    reset(mockConnector)

  "ReferenceDataService" - {

    "getCustomsOfficeByCode should" - {
      "return a customsOffice" in {

        when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCustomsOfficeByCode("GB00001").futureValue mustBe
          Some(customsOffice)

        verify(mockConnector).getCustomsOffice(any())(any(), any())
      }

      "return None if customsOffice can't be found" in {

        when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(None))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getCustomsOfficeByCode("GB00001").futureValue mustBe None
      }
    }

    "getControlType should" - {
      "return a controlType when typeofControl code is present in reference data" in {

        when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(controlTypeForValidCode))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getControlType("44").futureValue mustBe controlTypeForValidCode

        verify(mockConnector).getControlType(any())(any(), any())
      }

      "return a controlType when typeofControl code is not present in reference data" in {

        when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(controlTypeForInvalidCode))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getControlType("999").futureValue mustBe controlTypeForInvalidCode

        verify(mockConnector).getControlType(any())(any(), any())
      }
    }

    "getFunctionalError should" - {
      "return a functionalError when error code is present in reference data" in {

        when(mockConnector.getFunctionalErrorDescription(any())(any(), any())).thenReturn(Future.successful(functionalErrorForValidCode))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getFunctionalErrorType("14").futureValue mustBe functionalErrorForValidCode

        verify(mockConnector).getFunctionalErrorDescription(any())(any(), any())
      }

      "return a controlType when typeofControl code is not present in reference data" in {

        when(mockConnector.getFunctionalErrorDescription(any())(any(), any())).thenReturn(Future.successful(functionalErrorForInValidCode))

        val service = new ReferenceDataServiceImpl(mockConnector)

        service.getFunctionalErrorType("999").futureValue mustBe functionalErrorForInValidCode

        verify(mockConnector).getFunctionalErrorDescription(any())(any(), any())
      }
    }

  }

}
