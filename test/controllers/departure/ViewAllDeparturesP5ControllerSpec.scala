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
import models.LocalReferenceNumber
import models.departureP5.DepartureMessageType.DepartureNotification
import models.departureP5._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.ListPaginationViewModel
import views.html.departureP5.ViewAllDeparturesP5View

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
      .p5GuiceApplicationBuilder()
      .overrides(
        bind[DepartureMovementP5Connector].toInstance(mockDepartureMovementConnector),
        bind[DepartureP5MessageService].toInstance(mockDepartureMovementService)
      )

  val dateTime: LocalDateTime = LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)

  val departureMovement: DepartureMovement = DepartureMovement(
    "63651574c3447b12",
    None,
    LocalReferenceNumber("AB123"),
    dateTime
  )

  val mockDepartureMovementResponse: DepartureMovements = DepartureMovements(
    departureMovements = Seq(departureMovement),
    totalCount = 1
  )

  val mockDepartureMessageResponse: MessagesForDepartureMovement = MessagesForDepartureMovement(
    NonEmptyList(
      DepartureMessage(
        "messageId",
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
    referenceNumber = lrn.value,
    status = "movement.status.P5.departureNotificationSubmitted",
    actions = Nil
  )

  "ViewAllDepartures Controller" - {

    "return OK and the correct view for a GET" - {

      "when there is no search param or page" in {
        when(mockDepartureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
          .thenReturn(Future.successful(Some(mockDepartureMovementResponse)))

        when(mockDepartureMovementService.getLatestMessagesForMovement(any())(any(), any()))
          .thenReturn(
            Future.successful(
              Seq(
                OtherMovementAndMessage(
                  departureIdP5,
                  lrn,
                  dateTime,
                  LatestDepartureMessage(
                    DepartureMessage(
                      "messageId",
                      dateTime,
                      DepartureNotification,
                      "body/path"
                    ),
                    "ie015MessageId"
                  )
                )
              )
            )
          )

        val request = FakeRequest(GET, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url)

        val result = route(app, request).value

        val view = injector.instanceOf[ViewAllDeparturesP5View]

        status(result) mustEqual OK

        val expectedPaginationViewModel = ListPaginationViewModel(
          totalNumberOfItems = mockDepartureMovementResponse.movements.length,
          currentPage = 1,
          numberOfItemsPerPage = paginationAppConfig.departuresNumberOfMovements,
          href = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
        )
        val expectedViewModel = ViewAllDepartureMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel, None)

        contentAsString(result) mustEqual
          view(form, expectedViewModel)(request, messages).toString

        verify(mockDepartureMovementConnector).getAllMovementsForSearchQuery(eqTo(1), eqTo(paginationAppConfig.departuresNumberOfMovements), eqTo(None))(any())
      }

      "when there is a search param and page defined" in {
        val searchParam = "LRN123"
        val currentPage = Gen.chooseNum(2, 10: Int).sample.value

        when(mockDepartureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
          .thenReturn(Future.successful(Some(mockDepartureMovementResponse)))

        when(mockDepartureMovementService.getLatestMessagesForMovement(any())(any(), any()))
          .thenReturn(
            Future.successful(
              Seq(
                OtherMovementAndMessage(
                  departureIdP5,
                  lrn,
                  dateTime,
                  LatestDepartureMessage(
                    DepartureMessage(
                      "messageId",
                      dateTime,
                      DepartureNotification,
                      "body/path"
                    ),
                    "ie015MessageId"
                  )
                )
              )
            )
          )

        val request = FakeRequest(GET, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(Some(currentPage), Some(searchParam)).url)

        val result = route(app, request).value

        val filledForm = form.bind(Map("value" -> searchParam))

        val view = injector.instanceOf[ViewAllDeparturesP5View]

        status(result) mustEqual OK

        val expectedPaginationViewModel = ListPaginationViewModel(
          totalNumberOfItems = mockDepartureMovementResponse.movements.length,
          currentPage = currentPage,
          numberOfItemsPerPage = paginationAppConfig.departuresNumberOfMovements,
          href = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
        )
        val expectedViewModel = ViewAllDepartureMovementsP5ViewModel(Seq(mockViewMovement), expectedPaginationViewModel, Some(searchParam))

        contentAsString(result) mustEqual
          view(filledForm, expectedViewModel)(request, messages).toString

        verify(mockDepartureMovementConnector).getAllMovementsForSearchQuery(
          eqTo(currentPage),
          eqTo(paginationAppConfig.departuresNumberOfMovements),
          eqTo(Some(searchParam))
        )(any())
      }
    }

    "redirect to technical difficulties when no movements found" in {

      when(mockDepartureMovementConnector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }

}
