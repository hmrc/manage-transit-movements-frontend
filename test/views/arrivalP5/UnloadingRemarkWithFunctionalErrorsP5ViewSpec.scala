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

package views.arrivalP5

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.arrivalP5.UnloadingRemarkWithFunctionalErrorsP5View

class UnloadingRemarkWithFunctionalErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val prefix: String = "arrival.ie057.review.unloading.message"

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))

  val tableRows: Seq[TableRow]       = arbitrary[Seq[TableRow]].sample.value
  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val viewModel: UnloadingRemarkWithFunctionalErrorsP5ViewModel =
    new UnloadingRemarkWithFunctionalErrorsP5ViewModel(Seq(tableRows), mrn, false)

  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfErrorsPerPage

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: UnloadingRemarkWithFunctionalErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingRemarkWithFunctionalErrorsP5View]
      .apply(viewModel, arrivalIdP5, messageId, paginationViewModel)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(viewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(viewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithPagination(controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url)

  behave like pageWithTable()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"MRN: $mrn")

  behave like pageWithSpecificContent(
    "paragraph-1",
    "There is a problem with the unloading remarks for this notification. Review the error and try making the unloading remarks again."
  )

  behave like pageWithLink(
    "helpdesk-link",
    "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithSubmitButton("Make unloading remarks")

  behave like pageWithLink(
    "arrival-link",
    "View arrival notifications",
    controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
  )

}
