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

package controllers.departureP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import connectors.DepartureMovementP5Connector
import forms.DeparturesSearchFormProvider
import generators.Generators
import models.MessageStatus
import models.departureP5.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureP5MessageService
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import views.html.departureP5.ViewAllDeparturesP5View

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllDeparturesP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val departureMovementP5Connector = mock[DepartureMovementP5Connector]
  private val departureP5MessageService    = mock[DepartureP5MessageService]

  private val formProvider = new DeparturesSearchFormProvider()
  private val form         = formProvider()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(departureMovementP5Connector)
    reset(departureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMovementP5Connector].toInstance(departureMovementP5Connector))
      .overrides(bind[DepartureP5MessageService].toInstance(departureP5MessageService))

  "ViewAllDeparturesP5Controller" - {

    "must return OK for a GET" in {
      val movement = DepartureMovement("id", Some("mrn"), "ref", LocalDateTime.now())
      when(departureMovementP5Connector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(DepartureMovements(Seq(movement), 1))))
      when(departureP5MessageService.getLatestMessagesForMovements(any())(any(), any()))
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

      val controllerUrl = routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      status(result) mustEqual OK
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
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

      val departures = movementsAndMessages.map(ViewDepartureP5(_))

      when(departureMovementP5Connector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(movements)))

      when(departureP5MessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(Future.successful(movementsAndMessages))

      val searchParam = "§§§"

      val filledForm = form.bind(Map("value" -> searchParam))

      val controllerUrl = routes.ViewAllDeparturesP5Controller.onPageLoad(None, Some(searchParam)).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      val view      = injector.instanceOf[ViewAllDeparturesP5View]
      val viewModel = ViewAllDepartureMovementsP5ViewModel(departures, None, 1, 20, movements.totalCount)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual
        view(filledForm, viewModel)(request, messages).toString

      verify(departureMovementP5Connector).getAllMovementsForSearchQuery(any(), any(), eqTo(None))(any())
    }
  }
}
