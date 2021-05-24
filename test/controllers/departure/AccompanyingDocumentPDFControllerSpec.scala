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

package controllers.departure

import akka.util.ByteString
import base.SpecBase
import connectors.{BetaAuthorizationConnector, DeparturesMovementConnector}
import controllers.departure.{routes => departureRoutes}
import generators.Generators
import matchers.JsonMatchers.containJson
import models.DepartureId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class AccompanyingDocumentPDFControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val wsResponse: AhcWSResponse                            = mock[AhcWSResponse]
  val mockDeparturesMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]
  private val mockBetaAuthorizationConnector                       = mock[BetaAuthorizationConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(wsResponse)
    reset(mockDeparturesMovementConnector)
    reset(mockBetaAuthorizationConnector)
  }

  private val appBuilder =
    applicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDeparturesMovementConnector),
        bind[BetaAuthorizationConnector].toInstance(mockBetaAuthorizationConnector)
      )

  "AccompanyingDocumentPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {

        forAll(arbitrary[Array[Byte]] suchThat (_.nonEmpty)) {
          pdf =>
            when(wsResponse.status) thenReturn 200
            when(wsResponse.bodyAsBytes) thenReturn ByteString(pdf)
            when(mockBetaAuthorizationConnector.getBetaUser(any())(any()))
              .thenReturn(Future.successful(true))

            when(mockDeparturesMovementConnector.getPDF(any())(any()))
              .thenReturn(Future.successful(wsResponse))

            val departureId = DepartureId(0)

            val application = appBuilder.build()

            val request = FakeRequest(GET, departureRoutes.AccompanyingDocumentPDFController.getPDF(departureId).url)
              .withSession("authToken" -> "BearerToken")

            running(application) {

              val result = route(application, request).value

              status(result) mustEqual OK
            }
        }
      }

      "must redirect to TechnicalDifficultiesController if connector returns error" in {
        val errorCode = Gen.oneOf(300, 500).sample.value

        val wsResponse: AhcWSResponse = mock[AhcWSResponse]
        when(wsResponse.status) thenReturn errorCode
        when(mockBetaAuthorizationConnector.getBetaUser(any())(any()))
          .thenReturn(Future.successful(true))

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        when(mockDeparturesMovementConnector.getPDF(any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val departureId = DepartureId(0)

        val application = appBuilder.build()

        val request        = FakeRequest(GET, departureRoutes.AccompanyingDocumentPDFController.getPDF(departureId).url)
        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val expectedJson = Json.obj("nctsEnquiries" -> frontendAppConfig.nctsEnquiriesUrl)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        templateCaptor.getValue mustEqual "technicalDifficulties.njk"
        jsonCaptor.getValue must containJson(expectedJson)
        application.stop()
      }

      "must redirect to OldInterstitialController if user is not part of the private beta list" in {
        when(mockBetaAuthorizationConnector.getBetaUser(any())(any()))
          .thenReturn(Future.successful(false))

        val departureId = DepartureId(0)

        val application = appBuilder.build()

        val request = FakeRequest(GET, departureRoutes.AccompanyingDocumentPDFController.getPDF(departureId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.OldServiceInterstitialController.onPageLoad().url)

        application.stop()
      }
    }
  }
}
