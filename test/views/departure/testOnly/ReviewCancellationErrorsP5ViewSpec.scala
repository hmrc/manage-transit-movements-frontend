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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, SummaryListViewBehaviours}
import views.html.departure.TestOnly.ReviewCancellationErrorsP5View

class ReviewCancellationErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with SummaryListViewBehaviours with Generators {

  override val prefix: String = "departure.ie056.review.cancellation.message"

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(2, 1, 2, "test")

  private val reviewRejectionMessageP5ViewModel: ReviewCancellationErrorsP5ViewModel =
    new ReviewCancellationErrorsP5ViewModel(sections, lrn.toString, false)

  private def applyView(
    reviewRejectionViewModel: ReviewCancellationErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewCancellationErrorsP5View]
      .apply(reviewRejectionViewModel, departureId.toString, paginationViewModel)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(reviewRejectionMessageP5ViewModel, paginationViewModel)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId.toString, messageId, lrn).url)

  behave like pageWithSummaryLists()

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("h2", sectionTitle)
    })
  }

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
    "View departure declarations",
    controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
  )
}
