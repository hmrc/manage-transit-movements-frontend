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
import forms.ArrivalsSearchFormProvider
import generators.Generators
import models.arrivalP5._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ArrivalP5MessageService
import viewModels.P5.arrival.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.pagination.MovementsPaginationViewModel
import views.html.arrival.P5.ViewAllArrivalsP5View

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class ViewAllArrivalsP5ControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalMovementConnector = mock[ArrivalMovementP5Connector]
  private val mockArrivalMovementService   = mock[ArrivalP5MessageService]

  private val formProvider = new ArrivalsSearchFormProvider()
  private val form         = formProvider()

  override def beforeEach(): Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockArrivalMovementService)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementP5Connector].toInstance(mockArrivalMovementConnector),
        bind[ArrivalP5MessageService].toInstance(mockArrivalMovementService)
      )

  val dateTime: LocalDateTime = LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)

  val arrivalMovement: ArrivalMovement = ArrivalMovement(
    "63651574c3447b12",
    mrn,
    dateTime,
    "movements/arrivals/63651574c3447b12/messages"
  )

  val mockArrivalMovementResponse: ArrivalMovements = ArrivalMovements(
    arrivalMovements = Seq(arrivalMovement),
    totalCount = 1
  )

  val mockArrivalMessageResponse: MessagesForArrivalMovement = MessagesForArrivalMovement(
    NonEmptyList(
      ArrivalMessage(
        messageId,
        dateTime,
        ArrivalMessageType.ArrivalNotification
      ),
      List.empty[ArrivalMessage]
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

    "return OK and the correct view for a GET" - {

      "when there is no search param or page" in {

        when(mockArrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
          .thenReturn(Future.successful(Some(mockArrivalMovementResponse)))

        when(mockArrivalMovementConnector.getMessagesForMovement(any())(any()))
          .thenReturn(Future.successful(mockArrivalMessageResponse))

        when(mockArrivalMovementService.getMessagesForAllMovements(any())(any(), any()))
          .thenReturn(
            Future.successful(
              Seq(ArrivalMovementAndMessage(arrivalMovement, mockArrivalMessageResponse, 0))
            )
          )

        val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url)

        val result = route(app, request).value

        val view = injector.instanceOf[ViewAllArrivalsP5View]

        status(result) mustEqual OK

        val expectedPaginationViewModel = MovementsPaginationViewModel(
          totalNumberOfMovements = mockArrivalMovementResponse.movements.length,
          currentPage = 1,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
        )
        val expectedViewModel = ViewAllArrivalMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

        contentAsString(result) mustEqual
          view(form, expectedViewModel)(request, messages).toString

        verify(mockArrivalMovementConnector).getAllMovementsForSearchQuery(eqTo(1), eqTo(paginationAppConfig.departuresNumberOfMovements), eqTo(None))(any())
      }

      "when there is a search param and page defined" in {
        val searchParam = "MRN123"
        val currentPage = Gen.chooseNum(2, 10: Int).sample.value

        when(mockArrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
          .thenReturn(Future.successful(Some(mockArrivalMovementResponse)))

        when(mockArrivalMovementConnector.getMessagesForMovement(any())(any()))
          .thenReturn(Future.successful(mockArrivalMessageResponse))

        when(mockArrivalMovementService.getMessagesForAllMovements(any())(any(), any()))
          .thenReturn(
            Future.successful(
              Seq(ArrivalMovementAndMessage(arrivalMovement, mockArrivalMessageResponse, 0))
            )
          )

        val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(Some(currentPage), Some(searchParam)).url)

        val result = route(app, request).value

        val filledForm = form.bind(Map("value" -> searchParam))

        val view = injector.instanceOf[ViewAllArrivalsP5View]

        status(result) mustEqual OK

        val expectedPaginationViewModel = MovementsPaginationViewModel(
          totalNumberOfMovements = mockArrivalMovementResponse.movements.length,
          currentPage = currentPage,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
        )
        val expectedViewModel = ViewAllArrivalMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

        contentAsString(result) mustEqual
          view(filledForm, expectedViewModel)(request, messages).toString

        verify(mockArrivalMovementConnector).getAllMovementsForSearchQuery(
          eqTo(currentPage),
          eqTo(paginationAppConfig.departuresNumberOfMovements),
          eqTo(Some(searchParam))
        )(any())
      }
    }

    "redirect to technical difficulties when no movements found" in {

      when(mockArrivalMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }

}
