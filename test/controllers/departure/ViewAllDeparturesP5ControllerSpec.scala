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

package controllers.departure

import base.SpecBase
import cats.data.NonEmptyList
import connectors.DepartureMovementP5Connector
import forms.DeparturesSearchFormProvider
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
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.ViewMovementAction
import viewModels.pagination.MovementsPaginationViewModel
import views.html.departure.TestOnly.ViewAllDeparturesP5View

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class ViewAllDeparturesP5ControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMovementConnector = mock[DepartureMovementP5Connector]
  private val mockDepartureMovementService   = mock[DepartureP5MessageService]

  private val formProvider = new DeparturesSearchFormProvider()
  private val form         = formProvider()

  override def beforeEach(): Unit = {
    reset(mockDepartureMovementConnector)
    reset(mockDepartureMovementService)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMovementP5Connector].toInstance(mockDepartureMovementConnector),
        bind[DepartureP5MessageService].toInstance(mockDepartureMovementService)
      )

  val dateTime: LocalDateTime = LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)

  val departureMovement: DepartureMovement = DepartureMovement(
    "63651574c3447b12",
    None,
    dateTime,
    "movements/departures/63651574c3447b12/messages"
  )

  val mockDepartureMovementResponse: DepartureMovements = DepartureMovements(
    Seq(departureMovement)
  )

  val mockDepartureMessageResponse: MessagesForDepartureMovement = MessagesForDepartureMovement(
    NonEmptyList(
      DepartureMessage(
        departureIdP5,
        dateTime,
        DepartureMessageType.DepartureNotification,
        "body/path"
      ),
      List.empty[DepartureMessage]
    )
  )

  private val mockViewMovement = ViewDepartureP5(
    updatedDate = dateTime.toLocalDate,
    updatedTime = dateTime.toLocalTime,
    referenceNumber = "AB123",
    status = "movement.status.P5.departureNotificationSubmitted",
    actions = Seq(
      ViewMovementAction(s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}", "movement.status.P5.action.departureNotification.cancelDeclaration")
    )
  )

  "ViewAllDepartures Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockDepartureMovementConnector.getAllMovements()(any()))
        .thenReturn(Future.successful(Some(mockDepartureMovementResponse)))

      when(mockDepartureMovementService.getMessagesForAllMovements(any())(any(), any()))
        .thenReturn(
          Future.successful(Seq(DepartureMovementAndMessage(departureMovement, mockDepartureMessageResponse, "AB123", isDeclarationAmendable = true)))
        )

      val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad().url)

      val result = route(app, request).value

      val view = injector.instanceOf[ViewAllDeparturesP5View]

      status(result) mustEqual OK

      val expectedPaginationViewModel = MovementsPaginationViewModel(
        totalNumberOfMovements = mockDepartureMovementResponse.movements.length,
        currentPage = 1,
        numberOfMovementsPerPage = paginationAppConfig.departuresNumberOfMovements,
        href = controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad().url
      )
      val expectedViewModel = ViewAllDepartureMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

      contentAsString(result) mustEqual
        view(form, expectedViewModel)(request, messages).toString
    }

    "redirect to technical difficulties when no movements found" in {

      when(mockDepartureMovementConnector.getAllMovements()(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }

}
