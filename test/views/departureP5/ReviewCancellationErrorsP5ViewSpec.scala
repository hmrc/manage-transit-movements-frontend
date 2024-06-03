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

package views.departureP5

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.ReviewCancellationErrorsP5View

class ReviewCancellationErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val prefix: String = "departure.ie056.review.cancellation.message"

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))
  val tableRows: Seq[TableRow] = arbitrary[Seq[TableRow]].sample.value

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(2, 1, 2, "test")

  private val reviewRejectionMessageP5ViewModel: ReviewCancellationErrorsP5ViewModel =
    new ReviewCancellationErrorsP5ViewModel(Seq(tableRows), lrn.toString, false)

  private def applyView(
    reviewRejectionViewModel: ReviewCancellationErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewCancellationErrorsP5View]
      .apply(reviewRejectionViewModel, departureId.toString, paginationViewModel)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(controllers.departureP5.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url)

  behave like pageWithTable()

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1",
      s"The office of departure was not able to cancel this declaration. Review the error - then if you still want to cancel the declaration, try cancelling it again."
    )
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "helpdesk",
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
    "View departure declarations",
    controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
  )
}
