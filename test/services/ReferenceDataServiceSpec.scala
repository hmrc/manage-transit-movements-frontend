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
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.referenceData.{ControlType, CustomsOffice, FunctionalErrorWithDesc, RequestedDocumentType}
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
  private val customsOffices  = Seq(customsOffice1, customsOffice2)

  private val controlTypeCode    = "1"
  private val controlType1       = ControlType(controlTypeCode, "CT1")
  private val controlType2       = ControlType("2", "CT2")
  private val defaultControlType = ControlType(controlTypeCode, "")
  private val controlTypes       = Seq(controlType1, controlType2)

  private val requestedDocumentTypeCode    = "C620"
  private val requestedDocumentType1       = RequestedDocumentType(requestedDocumentTypeCode, "T2FL document")
  private val requestedDocumentType2       = RequestedDocumentType("C612", "Internal Community transit declaration, T2F")
  private val defaultRequestedDocumentType = RequestedDocumentType(requestedDocumentTypeCode, "")
  private val requestedDocumentTypes       = Seq(requestedDocumentType1, requestedDocumentType2)

  private val functionalErrorCode    = "1"
  private val functionalError1       = FunctionalErrorWithDesc(functionalErrorCode, "FE1")
  private val functionalError2       = FunctionalErrorWithDesc("2", "FE2")
  private val defaultFunctionalError = FunctionalErrorWithDesc(functionalErrorCode, "")
  private val functionalErrors       = Seq(functionalError1, functionalError2)

  private val defaultFunctionalErrors = Nil

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  "ReferenceDataService" - {

    "getCustomsOfficeByCode" - {

      val expectedQueryParams = Seq("data.id" -> customsOfficeId)

      "should return customs office" - {
        "when the customs office is found" in {
          when(mockConnector.getCustomsOffices(any())(any(), any())).thenReturn(Future.successful(customsOffices))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCustomsOffice(customsOfficeId).futureValue mustBe Right(customsOffice1)

          verify(mockConnector).getCustomsOffices(eqTo(expectedQueryParams))(any(), any())
        }
      }

      "should return Left" - {
        "when the customs office can't be found" in {
          when(mockConnector.getCustomsOffices(any())(any(), any())).thenReturn(Future.successful(Nil))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCustomsOffice(customsOfficeId).futureValue mustBe Left(customsOfficeId)

          verify(mockConnector).getCustomsOffices(eqTo(expectedQueryParams))(any(), any())
        }

        "when the connector call returns no data" in {
          when(mockConnector.getCustomsOffices(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getCustomsOffice(customsOfficeId).futureValue mustBe Left(customsOfficeId)

          verify(mockConnector).getCustomsOffices(eqTo(expectedQueryParams))(any(), any())
        }
      }
    }

    "getControlType" - {

      val expectedQueryParams = Seq("data.code" -> controlTypeCode)

      "should return a control type" - {
        "when the control type is found" in {
          when(mockConnector.getControlTypes(any())(any(), any())).thenReturn(Future.successful(controlTypes))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe controlType1

          verify(mockConnector).getControlTypes(eqTo(expectedQueryParams))(any(), any())
        }
      }

      "should return default" - {
        "when the control type can't be found" in {
          when(mockConnector.getControlTypes(any())(any(), any())).thenReturn(Future.successful(Nil))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe defaultControlType

          verify(mockConnector).getControlTypes(eqTo(expectedQueryParams))(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getControlTypes(any())(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getControlType(controlTypeCode).futureValue mustBe defaultControlType

          verify(mockConnector).getControlTypes(eqTo(expectedQueryParams))(any(), any())
        }
      }
    }

    "getRequestedDocumentType" - {

      val expectedQueryParams = Seq("data.code" -> requestedDocumentTypeCode)

      "should return a requested document type" - {
        "when the requested document type is found" in {
          when(mockConnector.getRequestedDocumentTypes(any())(any(), any())).thenReturn(Future.successful(requestedDocumentTypes))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getRequestedDocumentType(requestedDocumentTypeCode).futureValue mustBe requestedDocumentType1

          verify(mockConnector).getRequestedDocumentTypes(eqTo(expectedQueryParams))(any(), any())
        }
      }

      "should return default" - {
        "when the requested document type can't be found" in {
          when(mockConnector.getRequestedDocumentTypes(any())(any(), any())).thenReturn(Future.successful(Nil))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getRequestedDocumentType(requestedDocumentTypeCode).futureValue mustBe defaultRequestedDocumentType

          verify(mockConnector).getRequestedDocumentTypes(eqTo(expectedQueryParams))(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getRequestedDocumentTypes(any())(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getRequestedDocumentType(requestedDocumentTypeCode).futureValue mustBe defaultRequestedDocumentType

          verify(mockConnector).getRequestedDocumentTypes(eqTo(expectedQueryParams))(any(), any())
        }
      }
    }

    "getFunctionalError" - {

      val expectedQueryParams = Seq("data.code" -> functionalErrorCode)

      "should return a functional error" - {
        "when the functional error is found" in {
          when(mockConnector.getFunctionalErrors(any())(any(), any())).thenReturn(Future.successful(functionalErrors))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalError(functionalErrorCode).futureValue mustBe functionalError1

          verify(mockConnector).getFunctionalErrors(eqTo(expectedQueryParams))(any(), any())
        }
      }

      "should return default" - {
        "when the functional error can't be found" in {
          when(mockConnector.getFunctionalErrors(any())(any(), any())).thenReturn(Future.successful(Nil))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalError(functionalErrorCode).futureValue mustBe defaultFunctionalError

          verify(mockConnector).getFunctionalErrors(eqTo(expectedQueryParams))(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getFunctionalErrors(any())(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalError(functionalErrorCode).futureValue mustBe defaultFunctionalError

          verify(mockConnector).getFunctionalErrors(eqTo(expectedQueryParams))(any(), any())
        }
      }
    }

    "getFunctionalErrors" - {

      "should return functional errors" - {
        "when functional errors found" in {
          when(mockConnector.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(functionalErrors))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrors().futureValue mustBe functionalErrors

          verify(mockConnector).getFunctionalErrors()(any(), any())
        }
      }

      "should return default" - {
        "when no functional errors found" in {
          when(mockConnector.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(Nil))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrors().futureValue mustBe defaultFunctionalErrors

          verify(mockConnector).getFunctionalErrors()(any(), any())
        }

        "when the call fails" in {
          when(mockConnector.getFunctionalErrors()(any(), any())).thenReturn(Future.failed(new Throwable()))

          val service = new ReferenceDataServiceImpl(mockConnector)

          service.getFunctionalErrors().futureValue mustBe defaultFunctionalErrors

          verify(mockConnector).getFunctionalErrors()(any(), any())
        }
      }
    }
  }
}
