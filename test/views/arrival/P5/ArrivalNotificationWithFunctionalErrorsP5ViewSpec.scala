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

package views.arrival.P5

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import viewModels.ErrorViewModel
import viewModels.ErrorViewModel.ErrorRow
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import views.behaviours.{ErrorRowViewBehaviours, PaginationViewBehaviours}
import views.html.arrival.P5.ArrivalNotificationWithFunctionalErrorsP5View

import scala.concurrent.ExecutionContext

class ArrivalNotificationWithFunctionalErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with ErrorRowViewBehaviours with Generators {
  override val errorRows: Seq[ErrorRow] = arbitrary[Seq[ErrorRow]].sample.value
  override val prefix: String           = "arrival.ie057.review.notification.message"
  override val movementsPerPage: Int    = paginationAppConfig.arrivalsNumberOfErrorsPerPage

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = errorRows.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5).url,
    additionalParams = Seq()
  )
  private val errorViewModel: ErrorViewModel = ErrorViewModel(errorRows)
  private val ec: ExecutionContext           = ExecutionContext.global

  private val arrivalNotificationWithFunctionalErrorsP5ViewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel =
    new ArrivalNotificationWithFunctionalErrorsP5ViewModel(mrn, false)

  override def view: HtmlFormat.Appendable = applyView(arrivalNotificationWithFunctionalErrorsP5ViewModel, paginationViewModel, errorViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(arrivalNotificationWithFunctionalErrorsP5ViewModel, paginationViewModel, errorViewModel)

  private def applyView(
    viewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel,
    errorViewModel: ErrorViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationWithFunctionalErrorsP5View]
      .apply(viewModel, arrivalIdP5, paginationViewModel, errorViewModel)(fakeRequest, messages, frontendAppConfig, ec)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"MRN: $mrn")

  behave like pageWithPagination(controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5).url)

  behave like pageWithErrorRows()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1",
      s"There is a problem with this notification. Review the error and make a new notification with the right information."
    )
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "paragraph-2",
      "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)."
    )
    assertSpecificElementContainsText(
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )

  }

  behave like pageWithLink(
    "helpdesk-link",
    "New Computerised Transit System helpdesk",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "arrival-link",
    "Make another arrival notification",
    frontendAppConfig.declareArrivalNotificationStartUrl
  )

}
