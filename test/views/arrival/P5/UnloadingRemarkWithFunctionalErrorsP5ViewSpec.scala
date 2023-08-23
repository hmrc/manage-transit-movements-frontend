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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, SummaryListViewBehaviours}
import views.html.arrival.P5.UnloadingRemarkWithFunctionalErrorsP5View

class UnloadingRemarkWithFunctionalErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with SummaryListViewBehaviours with Generators {

  override val prefix: String = "arrival.ie057.review.unloading.message"

  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val viewModel: UnloadingRemarkWithFunctionalErrorsP5ViewModel =
    new UnloadingRemarkWithFunctionalErrorsP5ViewModel(sections, mrn, false)

  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfErrorsPerPage

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.testOnly.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5).url,
    additionalParams = Seq()
  )

  private def applyView(
    viewModel: UnloadingRemarkWithFunctionalErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingRemarkWithFunctionalErrorsP5View]
      .apply(viewModel, arrivalIdP5, paginationViewModel)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(viewModel, paginationViewModel)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(viewModel, paginationViewModel)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithPagination(controllers.testOnly.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5).url)

  behave like pageWithSummaryLists()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(s"MRN: $mrn")

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
      s"There is a problem with the unloading remarks for this notification. Review the error and try making the unloading remarks again."
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
    "View arrival notifications",
    controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
  )

}
