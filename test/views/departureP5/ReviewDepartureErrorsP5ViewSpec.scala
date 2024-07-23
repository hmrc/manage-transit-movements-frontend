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
import models.departureP5.BusinessRejectionType
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.ReviewDepartureErrorsP5View

class ReviewDepartureErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val prefix: String = "departure.ie056.review.message"

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))
  val tableRows: Seq[TableRow]       = arbitrary[Seq[TableRow]].sample.value
  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val reviewRejectionMessageP5ViewModel =
    new ReviewDepartureErrorsP5ViewModel(Seq(tableRows), lrn.toString, false, BusinessRejectionType.DeclarationRejection)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: ReviewDepartureErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel,
    mrn: Option[String] = None
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewDepartureErrorsP5View]
      .apply(viewModel, departureId.toString, paginationViewModel, mrn)(fakeRequest, messages, frontendAppConfig)

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
    controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url
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
    "Make another departure declaration",
    frontendAppConfig.p5Departure
  )

  "must not render add another declaration link when isAmendmentJourney is true" in {
    val reviewRejectionMessageP5ViewModel =
      new ReviewDepartureErrorsP5ViewModel(Seq(tableRows), lrn.toString, false, BusinessRejectionType.AmendmentRejection)

    val doc: Document = parseView(applyView(reviewRejectionMessageP5ViewModel, paginationViewModel))
    assertNotRenderedById(doc, "departure-link")
  }

  "must not render mrn when None" in {
    val doc: Document = parseView(applyView(reviewRejectionMessageP5ViewModel, paginationViewModel, None))
    assertNotRenderedById(doc, "mrn")
  }

  "must render mrn when provided" in {
    val doc: Document = parseView(applyView(reviewRejectionMessageP5ViewModel, paginationViewModel, Some("mrn")))
    assertRenderedById(doc, "mrn")
  }
}
