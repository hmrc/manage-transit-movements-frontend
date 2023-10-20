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

package controllers.testOnly

import akka.stream.scaladsl.Source
import akka.util.ByteString
import base.SpecBase
import generators.Generators
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.HttpEntity
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ManageDocumentsService

import scala.concurrent.Future

class TransitAccompanyingDocumentControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val mockManageDocumentsService: ManageDocumentsService = mock[ManageDocumentsService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockManageDocumentsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(
        bind[ManageDocumentsService].toInstance(mockManageDocumentsService)
      )

  private lazy val getTADRoute = routes.TransitAccompanyingDocumentController.getTAD(departureIdP5, messageId).url

  "TransitAccompanyingDocumentController" - {

    "getTAD" - {

      "must return OK and PDF" in {

        val contentLength = 100
        val contentType   = "application/octet-stream"
        val entity        = HttpEntity.Streamed(Source.empty[ByteString], Some(contentLength), Some(contentType))

        when(mockManageDocumentsService.getTAD(any(), any())(any()))
          .thenReturn(Future.successful(Some(entity)))

        val request = FakeRequest(GET, getTADRoute)

        val result = route(app, request).value

        status(result) mustEqual OK

        verify(mockManageDocumentsService).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }

      "must redirect to TechnicalDifficultiesController if service returns None" in {

        when(mockManageDocumentsService.getTAD(any(), any())(any())) thenReturn Future.successful(None)

        val request = FakeRequest(GET, getTADRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

        verify(mockManageDocumentsService).getTAD(eqTo(departureIdP5), eqTo(messageId))(any())
      }
    }
  }
}
