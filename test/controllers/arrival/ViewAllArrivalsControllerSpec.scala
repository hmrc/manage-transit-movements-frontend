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

package controllers.arrival

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ArrivalMovementConnector
import forms.ArrivalsSearchFormProvider
import generators.Generators
import models.MessageStatus
import models.arrival.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ArrivalMessageService
import viewModels.arrival.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.html.arrival.ViewAllArrivalsView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllArrivalsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val arrivalMovementConnector = mock[ArrivalMovementConnector]
  private val arrivalMessageService    = mock[ArrivalMessageService]

  private val formProvider = new ArrivalsSearchFormProvider()
  private val form         = formProvider()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(arrivalMovementConnector)
    reset(arrivalMessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalMovementConnector].toInstance(arrivalMovementConnector))
      .overrides(bind[ArrivalMessageService].toInstance(arrivalMessageService))

  "ViewAllArrivalsController" - {

    "must return OK for a GET" in {
      val arrivalMovement = ArrivalMovement("arrivialId", "mrn", LocalDateTime.now())
      when(arrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(ArrivalMovements(Seq(arrivalMovement), 1))))
      when(arrivalMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Seq(
              OtherMovementAndMessage(
                arrivalMovement,
                LatestArrivalMessage(
                  ArrivalMessage(
                    "messageId",
                    LocalDateTime.now(),
                    ArrivalMessageType.ArrivalNotification,
                    MessageStatus.Success
                  ),
                  "id"
                )
              )
            )
          )
        )

      val controllerUrl = routes.ViewAllArrivalsController.onPageLoad(None, None).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      status(result) mustEqual OK
    }

    "must return a Bad Request and errors when invalid data is submitted (GET)" in {
      val movement = ArrivalMovement("arrivialId", "mrn", LocalDateTime.now())

      val movementsAndMessages: Seq[ArrivalMovementAndMessage] = Seq(
        OtherMovementAndMessage(
          movement,
          LatestArrivalMessage(
            ArrivalMessage(
              "messageId",
              LocalDateTime.now(),
              ArrivalMessageType.ArrivalNotification,
              MessageStatus.Success
            ),
            "id"
          )
        )
      )

      val movements = ArrivalMovements(Seq(movement), 1)

      val arrivals = movementsAndMessages.map(ViewArrival(_))

      when(arrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(movements)))

      when(arrivalMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(Future.successful(movementsAndMessages))

      val searchParam = "§§§"

      val filledForm = form.bind(Map("value" -> searchParam))

      val controllerUrl = routes.ViewAllArrivalsController.onPageLoad(None, Some(searchParam)).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      val view      = injector.instanceOf[ViewAllArrivalsView]
      val viewModel = ViewAllArrivalMovementsViewModel(arrivals, None, 1, 20, movements.totalCount)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual
        view(filledForm, viewModel)(request, messages).toString

      verify(arrivalMovementConnector).getAllMovementsForSearchQuery(any(), any(), eqTo(None))(any())
    }

    "must return a Bad Request and errors when invalid data is submitted (POST)" in {
      val movement = ArrivalMovement("arrivialId", "mrn", LocalDateTime.now())

      val movementsAndMessages: Seq[ArrivalMovementAndMessage] = Seq(
        OtherMovementAndMessage(
          movement,
          LatestArrivalMessage(
            ArrivalMessage(
              "messageId",
              LocalDateTime.now(),
              ArrivalMessageType.ArrivalNotification,
              MessageStatus.Success
            ),
            "id"
          )
        )
      )

      val movements = ArrivalMovements(Seq(movement), 1)

      val arrivals = movementsAndMessages.map(ViewArrival(_))

      when(arrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(movements)))

      when(arrivalMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(Future.successful(movementsAndMessages))

      val searchParam = "§§§"
      val page        = 2

      val filledForm = form.bind(Map("value" -> searchParam))

      val controllerUrl = routes.ViewAllArrivalsController.onSubmit(Some(page)).url

      val request = FakeRequest(POST, controllerUrl)
        .withFormUrlEncodedBody(("value", searchParam))

      val result = route(app, request).value

      val view      = injector.instanceOf[ViewAllArrivalsView]
      val viewModel = ViewAllArrivalMovementsViewModel(arrivals, None, page, 20, movements.totalCount)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual
        view(filledForm, viewModel)(request, messages).toString

      verify(arrivalMovementConnector).getAllMovementsForSearchQuery(any(), any(), eqTo(None))(any())
    }
  }
}
