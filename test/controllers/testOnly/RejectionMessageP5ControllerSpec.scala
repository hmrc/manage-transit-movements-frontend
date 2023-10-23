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
import controllers.actions.{DepartureRejectionMessageActionProvider, FakeDepartureRejectionMessageAction}
import generators.Generators
import models.RejectionType
import models.departureP5.DepartureMessageType.AllocatedMRN
import models.departureP5._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewModels.P5.departure.RejectionMessageP5ViewModel
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.html.departure.TestOnly.RejectionMessageP5View

import java.time.LocalDateTime
import scala.concurrent.Future

class RejectionMessageP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRejectionMessageP5ViewModelProvider   = mock[RejectionMessageP5ViewModelProvider]
  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockRejectionMessageActionProvider        = mock[DepartureRejectionMessageActionProvider]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]

  private val rejectionType: RejectionType = RejectionType.DeclarationRejection

  def rejectionMessageAction(departureIdP5: String, mockDepartureP5MessageService: DepartureP5MessageService, mockCacheService: DepartureCacheConnector): Unit =
    when(mockRejectionMessageActionProvider.apply(any(), any())) thenReturn new FakeDepartureRejectionMessageAction(
      departureIdP5,
      lrn,
      mockDepartureP5MessageService,
      mockCacheService
    )

  lazy val rejectionMessageController: String =
    controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn, isAmendmentJourney = false).url

  lazy val rejectionMessageControllerAmendment: String =
    controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn, isAmendmentJourney = true).url
  lazy val rejectionMessageOnAmend: String = controllers.testOnly.routes.RejectionMessageP5Controller.onAmend(departureIdP5, lrn).url
  val sections: Seq[Section]               = arbitrarySections.arbitrary.sample.value
  val tableRow: TableRow                   = arbitraryTableRow.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockRejectionMessageP5ViewModelProvider)
    reset(mockRejectionMessageActionProvider)
    reset(mockCacheService)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[RejectionMessageP5ViewModelProvider].toInstance(mockRejectionMessageP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))

  "RejectionMessageP5Controller" - {

    "must return OK and the correct view for a GET" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
          CustomsOfficeOfDeparture("AB123"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
      when(mockRejectionMessageP5ViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, multipleErrors = true, isAmendmentJourney = false)))
      when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
        .thenReturn(Future.successful(None))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val rejectionMessageP5ViewModel = new RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, true, isAmendmentJourney = false)

      val paginationViewModel = ListPaginationViewModel(
        totalNumberOfItems = message.data.functionalErrors.length,
        currentPage = 1,
        numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
        href = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn, isAmendmentJourney = false).url,
        additionalParams = Seq()
      )

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RejectionMessageP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5, paginationViewModel, lrn)(request, messages, frontendAppConfig).toString
    }

    "must return OK and the correct view for a GET when amendment journey and declaration is amendable" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
          CustomsOfficeOfDeparture("AB123"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
      when(mockRejectionMessageP5ViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, multipleErrors = true, isAmendmentJourney = true)))
      when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
        .thenReturn(Future.successful(None))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val rejectionMessageP5ViewModel = new RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, true, isAmendmentJourney = true)

      val paginationViewModel = ListPaginationViewModel(
        totalNumberOfItems = message.data.functionalErrors.length,
        currentPage = 1,
        numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
        href = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn, isAmendmentJourney = true).url,
        additionalParams = Seq()
      )

      val request = FakeRequest(GET, rejectionMessageControllerAmendment)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RejectionMessageP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5, paginationViewModel, lrn)(request, messages, frontendAppConfig).toString
    }

    "must return OK and the correct view for a GET when amendment journey and declaration is not amendable" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
          CustomsOfficeOfDeparture("AB123"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))
      when(mockRejectionMessageP5ViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, multipleErrors = true, isAmendmentJourney = true)))
      when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
        .thenReturn(Future.successful(None))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val rejectionMessageP5ViewModel = new RejectionMessageP5ViewModel(Seq(Seq(tableRow)), lrn.toString, true, isAmendmentJourney = true)

      val paginationViewModel = ListPaginationViewModel(
        totalNumberOfItems = message.data.functionalErrors.length,
        currentPage = 1,
        numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
        href = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, lrn, isAmendmentJourney = true).url,
        additionalParams = Seq()
      )

      val request = FakeRequest(GET, rejectionMessageControllerAmendment)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RejectionMessageP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5, paginationViewModel, lrn)(request, messages, frontendAppConfig).toString
    }

    "must redirect to session expired when declaration amendable is false" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
          CustomsOfficeOfDeparture("AB123"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))
      when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
        .thenReturn(Future.successful(None))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url // TODO: Change to generic error page

    }

    "onAmend" - {

      "must redirect to technical difficulties when declaration is not amendable" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
            CustomsOfficeOfDeparture("12345"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))
        when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
          .thenReturn(Future.successful(None))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }

      "must redirect to technical difficulties when there are no errors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
            CustomsOfficeOfDeparture("12345"),
            Seq.empty
          )
        )
        when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
          .thenReturn(Future.successful(None))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }

      "must redirect to declaration summary page on success of handleErrors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
            CustomsOfficeOfDeparture("12345"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockCacheService.handleErrors(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
          .thenReturn(Future.successful(None))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.departureFrontendTaskListUrl(lrn.value)

      }

      "must redirect to new local reference number on success of handleErrors and IE028 is present" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
            CustomsOfficeOfDeparture("12345"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        val departureMessageMetaData = Some(DepartureMessageMetaData(LocalDateTime.now(), AllocatedMRN, "foo"))

        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockCacheService.handleErrors(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
          .thenReturn(Future.successful(departureMessageMetaData))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.departureNewLocalReferenceNumberUrl(lrn.value)

      }

      "must redirect to technical difficulties on failure of handleErrors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
            CustomsOfficeOfDeparture("12345"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.filterForMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockCacheService.handleErrors(any(), any())(any())).thenReturn(Future.successful(false))
        when(mockDepartureP5MessageService.getSpecificMessageMetaData(any(), eqTo(AllocatedMRN))(any(), any()))
          .thenReturn(Future.successful(None))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }
    }

  }
}
