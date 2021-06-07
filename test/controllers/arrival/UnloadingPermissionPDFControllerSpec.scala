/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.arrival

import akka.util.ByteString
import base.SpecBase
import config.FrontendAppConfig
import connectors.ArrivalMovementConnector
import generators.Generators
import matchers.JsonMatchers.containJson
import models.ArrivalId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class UnloadingPermissionPDFControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val wsResponse: AhcWSResponse                      = mock[AhcWSResponse]
  val mockArrivalMovementConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(wsResponse)
    reset(mockArrivalMovementConnector)
  }

  private val appBuilder =
    applicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
      )

  "UnloadingPermissionPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {
        val pdfAsBytes: Array[Byte] = Seq.fill(10)(Byte.MaxValue).toArray

        val expectedHeaders = Map(CONTENT_TYPE -> Seq("application/pdf"), CONTENT_DISPOSITION -> Seq("unloading_permission_123"), "OtherHeader" -> Seq("value"))

        when(wsResponse.status) thenReturn 200
        when(wsResponse.bodyAsBytes) thenReturn ByteString(pdfAsBytes)
        when(wsResponse.headers) thenReturn expectedHeaders

        when(mockArrivalMovementConnector.getPDF(any(), any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val arrivalId = ArrivalId(0)

        val application = appBuilder.build()

        val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
          .withSession(("authToken" -> "BearerToken"))

        running(application) {

          val result = route(application, request).value

          status(result) mustEqual OK
          headers(result).get(CONTENT_TYPE).value mustEqual "application/pdf"
          headers(result).get(CONTENT_DISPOSITION).value mustBe "unloading_permission_123"
        }
      }

      "must redirect to UnauthorisedController if bearer token is missing" in {

        val arrivalId = ArrivalId(0)

        val application = appBuilder.build()

        val request = FakeRequest(
          GET,
          routes.UnloadingPermissionPDFController.getPDF(arrivalId).url
        )

        running(application) {

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.UnauthorisedController.onPageLoad().url
        }
      }

      "must render TechnicalDifficulties page if connector returns error" in {
        val config = app.injector.instanceOf[FrontendAppConfig]
        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val genErrorResponseCode = Gen.oneOf(300, 500).sample.value

        val wsResponse: AhcWSResponse = mock[AhcWSResponse]
        when(wsResponse.status) thenReturn genErrorResponseCode

        when(mockArrivalMovementConnector.getPDF(any(), any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val arrivalId      = ArrivalId(0)
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val application = appBuilder.build()

        val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
          .withSession(("authToken" -> "BearerToken"))

        running(application) {

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj {
            "contactUrl" -> config.nctsEnquiriesUrl
          }

          templateCaptor.getValue mustEqual "technicalDifficulties.njk"
          jsonCaptor.getValue must containJson(expectedJson)

        }
      }
    }
  }
}
