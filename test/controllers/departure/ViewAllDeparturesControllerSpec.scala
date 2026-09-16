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

package controllers.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import connectors.DepartureMovementConnector
import forms.DeparturesSearchFormProvider
import generators.Generators
import models.MessageStatus
import models.departure.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureMessageService
import viewModels.departure.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.html.departure.ViewAllDeparturesView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllDeparturesControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val departureMovementConnector = mock[DepartureMovementConnector]
  private val departureMessageService    = mock[DepartureMessageService]

  private val formProvider = new DeparturesSearchFormProvider()
  private val form         = formProvider()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(departureMovementConnector)
    reset(departureMessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMovementConnector].toInstance(departureMovementConnector))
      .overrides(bind[DepartureMessageService].toInstance(departureMessageService))

  "ViewAllDeparturesController" - {

    "must return OK for a GET" in {
      val movement = DepartureMovement("id", Some("mrn"), "ref", LocalDateTime.now())
      when(departureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(DepartureMovements(Seq(movement), 1))))
      when(departureMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Seq(
              OtherMovementAndMessages(
                "id",
                "ref",
                LocalDateTime.now(),
                DepartureMovementMessages(
                  NonEmptyList.one(
                    DepartureMessage(
                      "messageId",
                      LocalDateTime.now(),
                      DepartureMessageType.DepartureNotification,
                      MessageStatus.Success
                    )
                  ),
                  "id"
                )
              )
            )
          )
        )

      val controllerUrl = routes.ViewAllDeparturesController.onPageLoad(None, None).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      status(result) mustEqual OK
    }

    "must return a Bad Request and errors when invalid data is submitted (GET)" in {
      val movement = DepartureMovement("id", Some("mrn"), "ref", LocalDateTime.now())

      val movementsAndMessages: Seq[MovementAndMessages] = Seq(
        OtherMovementAndMessages(
          "id",
          "ref",
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                "messageId",
                LocalDateTime.now(),
                DepartureMessageType.DepartureNotification,
                MessageStatus.Success
              )
            ),
            "id"
          )
        )
      )

      val movements = DepartureMovements(Seq(movement), 1)

      val departures = movementsAndMessages.map(ViewDeparture(_))

      when(departureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(movements)))

      when(departureMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(Future.successful(movementsAndMessages))

      val searchParam = "§§§"

      val filledForm = form.bind(Map("value" -> searchParam))

      val controllerUrl = routes.ViewAllDeparturesController.onPageLoad(None, Some(searchParam)).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      val view      = injector.instanceOf[ViewAllDeparturesView]
      val viewModel = ViewAllDepartureMovementsViewModel(departures, None, 1, 20, movements.totalCount)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual
        view(filledForm, viewModel)(request, messages).toString

      verify(departureMovementConnector).getAllMovementsForSearchQuery(any(), any(), eqTo(None))(any())
    }

    "must return a Bad Request and errors when invalid data is submitted (POST)" in {
      val movement = DepartureMovement("id", Some("mrn"), "ref", LocalDateTime.now())

      val movementsAndMessages: Seq[MovementAndMessages] = Seq(
        OtherMovementAndMessages(
          "id",
          "ref",
          LocalDateTime.now(),
          DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                "messageId",
                LocalDateTime.now(),
                DepartureMessageType.DepartureNotification,
                MessageStatus.Success
              )
            ),
            "id"
          )
        )
      )

      val movements = DepartureMovements(Seq(movement), 1)

      val departures = movementsAndMessages.map(ViewDeparture(_))

      when(departureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(movements)))

      when(departureMessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(Future.successful(movementsAndMessages))

      val searchParam = "§§§"
      val page        = 2

      val filledForm = form.bind(Map("value" -> searchParam))

      val controllerUrl = routes.ViewAllDeparturesController.onSubmit(Some(page)).url

      val request = FakeRequest(POST, controllerUrl)
        .withFormUrlEncodedBody(("value", searchParam))

      val result = route(app, request).value

      val view      = injector.instanceOf[ViewAllDeparturesView]
      val viewModel = ViewAllDepartureMovementsViewModel(departures, None, page, 20, movements.totalCount)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual
        view(filledForm, viewModel)(request, messages).toString

      verify(departureMovementConnector).getAllMovementsForSearchQuery(any(), any(), eqTo(None))(any())
    }
  }
}
