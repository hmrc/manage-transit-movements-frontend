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
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.RejectionMessageP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.RejectionMessageP5View

class RejectionMessageP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val headCells: Seq[HeadCell] = Seq(HeadCell(Text("Error code")), HeadCell(Text("Reason")))
  override val tableRows: Seq[TableRow] = arbitrary[Seq[TableRow]].sample.value

  override val prefix: String = "departure.ie056.message"

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val rejectionMessageP5ViewModel: RejectionMessageP5ViewModel =
    new RejectionMessageP5ViewModel(Seq(tableRows), lrn.toString, false, isAmendmentJourney = false)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId, isAmendmentJourney = false).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: RejectionMessageP5ViewModel,
    paginationViewModel: ListPaginationViewModel,
    isAmendmentJourney: Boolean = false
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[RejectionMessageP5View]
      .apply(viewModel, departureIdP5, messageId, paginationViewModel, isAmendmentJourney)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(rejectionMessageP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(rejectionMessageP5ViewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithSubmitButton("Amend errors")

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(
    controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId, isAmendmentJourney = false).url
  )

  behave like pageWithTable()

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText("paragraph-1", s"There is a problem with this declaration. Amend the error and resend the declaration.")
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

  "must render correct link text" in {
    assertSpecificElementContainsText("create-another-declaration", "Or make another departure declaration")
  }

  behave like pageWithLink(
    "helpdesk-link",
    "New Computerised Transit System helpdesk",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "departure-link",
    "make another departure declaration",
    frontendAppConfig.declareDepartureStartWithLRNUrl
  )

  "must not render table headings when no table rows" in {

    val rejectionMessageP5ViewModel: RejectionMessageP5ViewModel =
      new RejectionMessageP5ViewModel(Nil, lrn.toString, false, isAmendmentJourney = true)

    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel))
    assertElementDoesNotExist(doc, "govuk-table__head")

  }

  "must not render add another declaration link when isAmendmentJourney is true" in {
    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel, isAmendmentJourney = true))
    assertNotRenderedById(doc, "departure-link")
  }

}
