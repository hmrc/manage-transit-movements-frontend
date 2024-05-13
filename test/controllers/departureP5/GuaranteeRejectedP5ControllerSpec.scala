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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.DepartureCacheConnector
import generated.CC055CType
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel
import views.html.departureP5.GuaranteeRejectedP5View

import scala.concurrent.Future

class GuaranteeRejectedP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]
  private val mockDepartureCacheConnector   = mock[DepartureCacheConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockDepartureCacheConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockDepartureCacheConnector))

  "GuaranteeRejected" - {

    lazy val controller = routes.GuaranteeRejectedP5Controller.onPageLoad(departureIdP5, messageId, lrn).url

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

            when(mockDepartureCacheConnector.doesDeclarationExist(any())(any())) thenReturn Future.successful(true)

            val viewModel = GuaranteeRejectedP5ViewModel(
              guaranteeReferences = message.GuaranteeReference,
              lrn = lrn,
              isAmendable = true,
              mrn = message.TransitOperation.MRN,
              acceptanceDate = message.TransitOperation.declarationAcceptanceDate
            )

            val request = FakeRequest(GET, controller)

            val result = route(app, request).value

            status(result) mustEqual OK

            val view = injector.instanceOf[GuaranteeRejectedP5View]

            contentAsString(result) mustEqual
              view(viewModel, departureIdP5, messageId)(request, messages).toString
        }
      }
    }

    "onAmend" - {

      lazy val controller = routes.GuaranteeRejectedP5Controller.onAmend(departureIdP5, messageId, lrn).url

      "must redirect to NewLocalReferenceNumber page on success" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

            when(mockDepartureCacheConnector.handleGuaranteeRejection(any())(any())) thenReturn Future.successful(true)

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe frontendAppConfig.departureAmendGuaranteeErrorsUrl(lrn.value, departureIdP5)
        }
      }

      "must redirect to technical difficulties page on failure" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

            when(mockDepartureCacheConnector.handleGuaranteeRejection(any())(any())) thenReturn Future.successful(false)

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.ErrorController.technicalDifficulties().url
        }
      }
    }
  }
}
