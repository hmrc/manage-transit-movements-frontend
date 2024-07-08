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

package controllers.departureP5

import base.SpecBase
import connectors.ManageDocumentsConnector
import generators.Generators
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.HeaderNames.{CONTENT_LENGTH, CONTENT_TYPE}
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{Await, Future}

class TransitAccompanyingDocumentControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val mockManageDocumentsConnector: ManageDocumentsConnector = mock[ManageDocumentsConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManageDocumentsConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(
        bind[ManageDocumentsConnector].toInstance(mockManageDocumentsConnector)
      )

  private lazy val getTADRoute = routes.TransitAccompanyingDocumentController.getTAD(departureIdP5, messageId).url

  private val thisContentLength      = 100
  private val thisContentType        = "application/octet-stream"
  private val thisContentDisposition = "TAD_123"

  private val responseHeaders = Seq(
    CONTENT_LENGTH      -> Seq(thisContentLength.toString),
    CONTENT_TYPE        -> Seq(thisContentType),
    CONTENT_DISPOSITION -> Seq(thisContentDisposition)
  ).toMap

  private val errorCode     = Gen.oneOf(400, 599).sample.value
  private val okResponse    = HttpResponse(OK, "body", responseHeaders)
  private val errorResponse = HttpResponse(errorCode, "")

  "TransitAccompanyingDocumentController" - {

    "getTAD" - {

      "must return OK and PDF" in {

        when(mockManageDocumentsConnector.getTAD(any(), any())(any()))
          .thenReturn(Future.successful(okResponse))

        val request = FakeRequest(GET, getTADRoute)

        val result = route(app, request).value

        def contentLength(of: Future[Result])(implicit timeout: Timeout): Option[Long] =
          Await.result(of, timeout.duration).body.contentLength

        status(result) mustEqual OK
        contentType(result).value mustEqual thisContentType
        contentLength(result).value mustEqual thisContentLength
        headers(result).get(CONTENT_DISPOSITION).value mustBe thisContentDisposition

        verify(mockManageDocumentsConnector).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }

      "must redirect to TechnicalDifficultiesController if connector returns error" in {

        when(mockManageDocumentsConnector.getTAD(any(), any())(any()))
          .thenReturn(Future.successful(errorResponse))

        val request = FakeRequest(GET, getTADRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

        verify(mockManageDocumentsConnector).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }
    }
  }
}
