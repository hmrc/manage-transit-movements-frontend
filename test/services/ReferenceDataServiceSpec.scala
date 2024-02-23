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
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc}
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
  private val customsOffice   = CustomsOffice(customsOfficeId, "CO1", None)

  private val controlTypeCode = "1"
  private val controlType     = ControlType(controlTypeCode, "CT1")

  private val functionalErrorCode = "1"
  private val functionalError1    = FunctionalErrorWithDesc(functionalErrorCode, "FE1")
  private val functionalError2    = FunctionalErrorWithDesc("2", "FE2")
  private val functionalErrors    = NonEmptySet.of(functionalError1, functionalError2)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "ReferenceDataService" - {

    "getCustomsOfficeByCode" - {

      "should return customs office" - {
        "when the customs office is found" in {
          when(mockConnector.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCustomsOffice(customsOfficeId).futureValue mustBe customsOffice

          verify(mockConnector).getCustomsOffice(eqTo(customsOfficeId))(any(), any())
        }
      }
    }

    "getControlType" - {

      "should return a control type" - {
        "when the control type is found" in {
          when(mockConnector.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe controlType

          verify(mockConnector).getControlType(eqTo(controlTypeCode))(any(), any())
        }
      }
    }

    "getFunctionalError" - {

      "should return a functional error" - {
        "when the functional error is found" in {
          when(mockConnector.getFunctionalError(any())(any(), any())).thenReturn(Future.successful(functionalError1))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalError(functionalErrorCode).futureValue mustBe functionalError1

          verify(mockConnector).getFunctionalError(eqTo(functionalErrorCode))(any(), any())
        }
      }
    }

    "getFunctionalErrors" - {

      "should return functional errors" - {
        "when functional errors found" in {
          when(mockConnector.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(functionalErrors))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrors().futureValue mustBe
            Seq(functionalError1, functionalError2)

          verify(mockConnector).getFunctionalErrors()(any(), any())
        }
      }
    }
  }
}
