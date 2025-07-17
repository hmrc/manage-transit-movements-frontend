/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.arrivalP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ManageDocumentsConnector
import generators.Generators
import org.apache.pekko.util.Timeout
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{Await, Future}

class UnloadingPermissionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  private val mockManageDocumentsConnector = mock[ManageDocumentsConnector]

  lazy val controller: String = controllers.arrivalP5.routes.UnloadingPermissionController.getUnloadingPermissionDocument(arrivalIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManageDocumentsConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ManageDocumentsConnector].toInstance(mockManageDocumentsConnector))

  private val cntntType        = "application/octet-stream"
  private val cntntLength      = 100
  private val cntntDisposition = "TAD_123"

  private val responseHeaders = Seq(
    CONTENT_LENGTH      -> Seq(cntntLength.toString),
    CONTENT_TYPE        -> Seq(cntntType),
    CONTENT_DISPOSITION -> Seq(cntntDisposition)
  ).toMap

  private val okResponse = HttpResponse(OK, "body", responseHeaders)

  "UnloadingPermissionController" - {
    "must return OK when documents requested" in {
      when(mockManageDocumentsConnector.getUnloadingPermission(any(), any())(any())).thenReturn(Future.successful(okResponse))

      val request = FakeRequest(GET, controller)

      val result = route(app, request).value

      def contentLength(of: Future[Result])(implicit timeout: Timeout): Option[Long] =
        Await.result(of, timeout.duration).body.contentLength

      status(result) mustEqual OK
      contentType(result).value mustEqual cntntType
      contentLength(result).value mustEqual cntntLength
      headers(result).get(CONTENT_DISPOSITION).value mustEqual cntntDisposition
    }

    "must redirect to TechnicalDifficultiesController if connector returns error" in {
      val errorCode     = Gen.oneOf(400, 599).sample.value
      val errorResponse = HttpResponse(errorCode, "")

      when(mockManageDocumentsConnector.getUnloadingPermission(any(), any())(any()))
        .thenReturn(Future.successful(errorResponse))

      val request = FakeRequest(GET, controller)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
