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
import viewModels.P5.departure.ReviewPrelodgedDeclarationErrorsP5ViewModel
import viewModels.pagination.PaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.ReviewPrelodgedDeclarationErrorsP5View

class ReviewPrelodgedDeclarationErrorsP5ViewSpec extends PaginationViewBehaviours[PaginationViewModel] with TableViewBehaviours with Generators {

  override val prefix: String = "prelodged.declaration.ie056.review.message"

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))

  val tableRows: Seq[TableRow]       = arbitrary[Seq[TableRow]].sample.value
  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val reviewPrelodgeRejectionMessageP5ViewModel =
    new ReviewPrelodgedDeclarationErrorsP5ViewModel(Seq(tableRows), lrn.toString, false)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  override val buildViewModel: (Int, Int, Int, String) => PaginationViewModel =
    PaginationViewModel(_, _, _, _)

  val paginationViewModel: PaginationViewModel = PaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.departureP5.routes.ReviewPrelodgedDeclarationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: ReviewPrelodgedDeclarationErrorsP5ViewModel,
    paginationViewModel: PaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewPrelodgedDeclarationErrorsP5View]
      .apply(viewModel, departureIdP5, paginationViewModel)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(reviewPrelodgeRejectionMessageP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable =
    applyView(reviewPrelodgeRejectionMessageP5ViewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(
    controllers.departureP5.routes.ReviewPrelodgedDeclarationErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url
  )

  behave like pageWithTable()

  behave like pageWithSpecificContent(
    "paragraph-1",
    "There is a problem with this declaration. Review the error and complete your pre-lodged declaration with the right information."
  )

  behave like pageWithLink(
    "helpdesk-link",
    "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "prelodge-declaration-link",
    "Complete pre-lodged declaration",
    frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)
  )
}
