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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ManageDocumentsConnector
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import play.api.http.HeaderNames.{CONTENT_LENGTH, CONTENT_TYPE}
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ManageDocumentsServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockManageDocumentsConnector: ManageDocumentsConnector = mock[ManageDocumentsConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManageDocumentsConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ManageDocumentsConnector].toInstance(mockManageDocumentsConnector)
      )

  private val service = app.injector.instanceOf[ManageDocumentsService]

  private val contentLength = 100
  private val contentType   = "application/octet-stream"

  private val headers = Seq(
    CONTENT_LENGTH -> Seq(contentLength.toString),
    CONTENT_TYPE   -> Seq(contentType)
  ).toMap

  private val errorCode     = Gen.oneOf(400, 599).sample.value
  private val okResponse    = HttpResponse(OK, "body", headers)
  private val errorResponse = HttpResponse(errorCode, "")

  "ManageDocumentsService" - {

    "getTAD" - {
      "when connector call succeeds" in {
        when(mockManageDocumentsConnector.getTAD(any(), any())(any()))
          .thenReturn(Future.successful(okResponse))

        val result = service.getTAD(departureIdP5, messageId).futureValue

        result.value.contentLength.value mustBe contentLength
        result.value.contentType.value mustBe contentType

        verify(mockManageDocumentsConnector).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }

      "when connector call fails" in {
        when(mockManageDocumentsConnector.getTAD(any(), any())(any()))
          .thenReturn(Future.successful(errorResponse))

        val result = service.getTAD(departureIdP5, messageId).futureValue

        result must not be defined

        verify(mockManageDocumentsConnector).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }
    }

    "getUnloadingPermission" - {
      "when connector call succeeds" in {
        when(mockManageDocumentsConnector.getUnloadingPermission(any(), any())(any()))
          .thenReturn(Future.successful(okResponse))

        val result = service.getUnloadingPermission(departureIdP5, messageId).futureValue

        result.value.contentLength.value mustBe contentLength
        result.value.contentType.value mustBe contentType

        verify(mockManageDocumentsConnector).getUnloadingPermission(eqTo(departureIdP5), eqTo(messageId))(any())
      }

      "when connector call fails" in {
        when(mockManageDocumentsConnector.getUnloadingPermission(any(), any())(any()))
          .thenReturn(Future.successful(errorResponse))

        val result = service.getUnloadingPermission(departureIdP5, messageId).futureValue

        result must not be defined

        verify(mockManageDocumentsConnector).getUnloadingPermission(eqTo(departureIdP5), eqTo(messageId))(any())
      }
    }
  }

}
