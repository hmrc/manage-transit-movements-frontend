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

package views.departure.testOnly

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departure.TestOnly.ReviewDepartureErrorsP5View

class ReviewDepartureErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val prefix: String           = "departure.ie056.review.message"
  override val headCells: Seq[HeadCell] = Seq(HeadCell(Text("Error code")), HeadCell(Text("Reason")))
  val tableRows: Seq[TableRow]          = arbitrary[Seq[TableRow]].sample.value
  private val sections: Seq[Section]    = arbitrary[List[Section]].sample.value

  private val reviewRejectionMessageP5ViewModel =
    new ReviewDepartureErrorsP5ViewModel(Seq(tableRows), lrn.toString, false, isAmendmentJourney = false)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId.toString, lrn, isAmendmentJourney = false).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: ReviewDepartureErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewDepartureErrorsP5View]
      .apply(viewModel, departureId.toString, paginationViewModel)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(
    controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId.toString, lrn, isAmendmentJourney = false).url
  )

  behave like pageWithTable()

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1-prefix",
      s"There is a problem with this declaration. Review the error and make a new declaration with the right information."
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
    "departure-link",
    "Make another departure declaration",
    frontendAppConfig.declareDepartureStartWithLRNUrl
  )
}
