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

package controllers.arrivalP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{CC057CType, FunctionalErrorType04}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ArrivalP5MessageService
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel.UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.html.arrivalP5.UnloadingRemarkWithFunctionalErrorsP5View

import scala.concurrent.Future

class UnloadingRemarkWithFunctionalErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReviewUnloadingRemarkErrorMessageP5ViewModelProvider = mock[UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider]
  private val mockArrivalP5MessageService                              = mock[ArrivalP5MessageService]

  lazy val controller: String = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url
  val sections: Seq[Section]  = arbitrarySections.arbitrary.sample.value
  val tableRow: TableRow      = arbitraryTableRow.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalP5MessageService)
    reset(mockReviewUnloadingRemarkErrorMessageP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider].toInstance(mockReviewUnloadingRemarkErrorMessageP5ViewModelProvider))
      .overrides(bind[ArrivalP5MessageService].toInstance(mockArrivalP5MessageService))

  "UnloadingRemarkWithFunctionalErrorsP5Controller" - {

    "must return OK and the correct view for a GET when functional errors are defined" in {
      forAll(listWithMaxLength[FunctionalErrorType04]()) {
        functionalErrors =>
          forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockArrivalP5MessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))
              when(mockReviewUnloadingRemarkErrorMessageP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(UnloadingRemarkWithFunctionalErrorsP5ViewModel(Seq(Seq(tableRow)), mrn, multipleErrors = true)))

              val paginationViewModel = ListPaginationViewModel(
                totalNumberOfItems = message.FunctionalError.length,
                currentPage = 1,
                numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
                href = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url,
                additionalParams = Seq()
              )

              val rejectionMessageP5ViewModel = new UnloadingRemarkWithFunctionalErrorsP5ViewModel(Seq(Seq(tableRow)), mrn, true)

              val request = FakeRequest(GET, controller)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[UnloadingRemarkWithFunctionalErrorsP5View]

              contentAsString(result) mustEqual
                view(rejectionMessageP5ViewModel, arrivalIdP5, messageId, paginationViewModel)(request, messages).toString
          }
      }
    }

    "must redirect to technical difficulties page when functionalErrors is 0" in {
      forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = Nil))) {
        message =>
          when(mockArrivalP5MessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))
          when(mockReviewUnloadingRemarkErrorMessageP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(UnloadingRemarkWithFunctionalErrorsP5ViewModel(Seq(Seq(tableRow)), mrn, multipleErrors = true)))

          val request = FakeRequest(GET, controller)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }
    }

    "must redirect to unloading remarks for a POST" in {
      forAll(arbitrary[CC057CType]) {
        message =>
          when(mockArrivalP5MessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          val request = FakeRequest(POST, controller)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual frontendAppConfig.p5UnloadingStart(arrivalIdP5, messageId)
      }
    }
  }
}
