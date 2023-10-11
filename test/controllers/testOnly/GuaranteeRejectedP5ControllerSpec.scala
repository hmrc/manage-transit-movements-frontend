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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.DepartureCacheConnector
import controllers.actions.{FakeGuaranteeRejectedAction, GuaranteeRejectedActionProvider}
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel
import views.html.departure.TestOnly.GuaranteeRejectedP5View

import java.time.LocalDate
import scala.concurrent.Future

class GuaranteeRejectedP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGuaranteeRejectedActionProvider = mock[GuaranteeRejectedActionProvider]
  private val mockDepartureP5MessageService       = mock[DepartureP5MessageService]
  private val mockDepartureCacheConnector         = mock[DepartureCacheConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockGuaranteeRejectedActionProvider)
    reset(mockDepartureCacheConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[GuaranteeRejectedActionProvider].toInstance(mockGuaranteeRejectedActionProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockDepartureCacheConnector))

  "GuaranteeRejected" - {

    "must return OK and the correct view for a GET" in {

      val controller: String =
        controllers.testOnly.routes.GuaranteeRejectedP5Controller.onPageLoad(departureIdP5, messageId, lrn).url

      val message: IE055Data = IE055Data(
        IE055MessageData(
          TransitOperationIE055("MRNCD3232", LocalDate.now()),
          Seq(
            GuaranteeReference(
              "AB123",
              Seq(InvalidGuaranteeReason("A", None))
            )
          )
        )
      )

      when(mockDepartureP5MessageService.getMessageWithMessageId[IE055Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(message))

      when(mockGuaranteeRejectedActionProvider.apply(any(), any())) thenReturn
        new FakeGuaranteeRejectedAction(departureIdP5, messageId, mockDepartureP5MessageService)

      when(mockDepartureCacheConnector.doesDeclarationExist(any())(any())) thenReturn Future.successful(true)

      val viewModel = GuaranteeRejectedP5ViewModel(
        message.data.guaranteeReferences,
        lrn,
        isAmendable = true,
        message.data.transitOperation.MRN,
        message.data.transitOperation.declarationAcceptanceDate
      )

      val request = FakeRequest(GET, controller)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[GuaranteeRejectedP5View]

      contentAsString(result) mustEqual
        view(viewModel)(request, messages).toString
    }

    "onAmend" - {

      "must redirect to NewLocalReferenceNumber page on success" in {

        val controller: String =
          controllers.testOnly.routes.GuaranteeRejectedP5Controller.onAmend(lrn).url

        when(mockDepartureCacheConnector.handleGuaranteeRejection(any())(any())) thenReturn Future.successful(true)

        val request = FakeRequest(GET, controller)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe frontendAppConfig.departureNewLocalReferenceNumberUrl(lrn.value)
      }

      "must redirect to technical difficulties page on failure" in {

        val controller: String =
          controllers.testOnly.routes.GuaranteeRejectedP5Controller.onAmend(lrn).url

        when(mockDepartureCacheConnector.handleGuaranteeRejection(any())(any())) thenReturn Future.successful(false)

        val request = FakeRequest(GET, controller)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
