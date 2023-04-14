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

package controllers.arrival

import base.SpecBase
import cats.data.NonEmptyList
import connectors.ArrivalMovementP5Connector
import forms.SearchFormProvider
import generators.Generators
import models.arrivalP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.P5.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.pagination.MovementsPaginationViewModel
import views.html.arrival.P5.ViewAllArrivalsP5View

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class ViewAllArrivalsP5ControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalMovementConnector = mock[ArrivalMovementP5Connector]

  private val formProvider = new SearchFormProvider()
  private val form         = formProvider("arrivals.search.form.value.invalid")

  override def beforeEach(): Unit = {
    reset(mockArrivalMovementConnector)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementP5Connector].toInstance(mockArrivalMovementConnector)
      )

  val dateTime = LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)

  val mockArrivalMovementResponse: ArrivalMovements = ArrivalMovements(
    Seq(
      ArrivalMovement(
        "63651574c3447b12",
        mrn,
        dateTime,
        "movements/arrivals/63651574c3447b12/messages"
      )
    )
  )

  val mockArrivalMessageResponse: MessagesForMovement = MessagesForMovement(
    NonEmptyList(
      Message(
        dateTime,
        ArrivalMessageType.ArrivalNotification
      ),
      List.empty[Message]
    )
  )

  private val mockViewMovement = ViewArrivalP5(
    updatedDate = dateTime.toLocalDate,
    updatedTime = dateTime.toLocalTime,
    movementReferenceNumber = mrn,
    status = "movement.status.P5.arrivalNotificationSubmitted",
    actions = Nil
  )

  "ViewAllArrivals Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockArrivalMovementConnector.getAllMovements()(any()))
        .thenReturn(Future.successful(Some(mockArrivalMovementResponse)))

      when(mockArrivalMovementConnector.getMessagesForMovement(any())(any()))
        .thenReturn(Future.successful(mockArrivalMessageResponse))

      val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url)

      val result = route(app, request).value

      val view = injector.instanceOf[ViewAllArrivalsP5View]

      status(result) mustEqual OK

      val expectedPaginationViewModel = MovementsPaginationViewModel(
        totalNumberOfMovements = mockArrivalMovementResponse.movements.length,
        currentPage = 1,
        numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
        href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url
      )
      val expectedViewModel = ViewAllArrivalMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

      contentAsString(result) mustEqual
        view(form, expectedViewModel)(request, messages).toString
    }

    "redirect to technical difficulties when no movements found" in {

      when(mockArrivalMovementConnector.getAllMovements()(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }

}
