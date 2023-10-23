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
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.DepartureCancelledP5ViewModel
import viewModels.P5.departure.DepartureCancelledP5ViewModel.DepartureCancelledP5ViewModelProvider
import views.html.departureP5.DepartureCancelledP5View

import java.time.LocalDateTime
import scala.concurrent.Future

class DepartureCancelledP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureCancelledP5ViewModelProvider = mock[DepartureCancelledP5ViewModelProvider]
  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockReferenceDataService                  = mock[ReferenceDataService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockReferenceDataService)
    reset(mockDepartureCancelledP5ViewModelProvider)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[DepartureCancelledP5ViewModelProvider].toInstance(mockDepartureCancelledP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  private val customsReferenceNumber = Gen.alphaNumStr.sample.value

  val departureCancelledController: String =
    controllers.departureP5.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, messageId).url

  "DepartureCancelledP5Controller" - {

    "must redirect to correct controller" - {
      "when decision is true" in {
        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "0",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))

        val request = FakeRequest(GET, departureCancelledController)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.departureP5.routes.DepartureCancelledP5Controller
          .declarationNotCancelled(departureIdP5, messageId)
          .url

      }

      "when decision is false" in {
        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "1",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))

        val request = FakeRequest(GET, departureCancelledController)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.departureP5.routes.DepartureCancelledP5Controller
          .declarationCancelled(departureIdP5, messageId)
          .url
      }
    }

    "must return OK and the correct view for a GET" - {

      "when decision is true" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "1",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        val departureCancelledP5ViewModel = new DepartureCancelledP5ViewModel(sections, lrn.toString, customsReferenceNumber, None, isCancelled = true)

        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))
        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockDepartureCancelledP5ViewModelProvider.apply(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(departureCancelledP5ViewModel))

        val request = FakeRequest(GET, controllers.departureP5.routes.DepartureCancelledP5Controller.declarationCancelled(departureIdP5, messageId).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = injector.instanceOf[DepartureCancelledP5View]

        contentAsString(result) mustEqual
          view(departureCancelledP5ViewModel)(request, messages, frontendAppConfig).toString
      }

      "when decision is false" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "0",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        val departureCancelledP5ViewModel = new DepartureCancelledP5ViewModel(sections, lrn.toString, customsReferenceNumber, None, isCancelled = false)

        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
          .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))
        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(None))
        when(mockDepartureCancelledP5ViewModelProvider.apply(any(), any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future.successful(departureCancelledP5ViewModel))

        val request = FakeRequest(GET, controllers.departureP5.routes.DepartureCancelledP5Controller.declarationNotCancelled(departureIdP5, messageId).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val view = injector.instanceOf[DepartureCancelledP5View]

        contentAsString(result) mustEqual
          view(departureCancelledP5ViewModel)(request, messages, frontendAppConfig).toString
      }

    }

  }
}
