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
import models.FunctionalError.FunctionalErrorWithSection
import models.FunctionalErrors.FunctionalErrorsWithSection
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.ReviewDepartureErrorsP5View

class ReviewDepartureErrorsP5ViewSpec
    extends PaginationViewBehaviours[FunctionalErrorWithSection, ReviewDepartureErrorsP5ViewModel]
    with TableViewBehaviours
    with Generators {

  override val viewModel: ReviewDepartureErrorsP5ViewModel =
    arbitraryReviewDepartureErrorsP5ViewModel.arbitrary.sample.value

  override val table: Table = viewModel.table

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): ReviewDepartureErrorsP5ViewModel =
    viewModel.copy(
      functionalErrors = {
        def error: FunctionalErrorWithSection = arbitrary[FunctionalErrorWithSection].sample.value
        FunctionalErrorsWithSection(Seq.fill(totalNumberOfItems)(error))
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage
    )

  override val prefix: String        = "departure.ie056.review.message"
  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  override def view: HtmlFormat.Appendable = applyView(viewModel, None)

  override def viewWithSpecificPagination(viewModel: ReviewDepartureErrorsP5ViewModel): HtmlFormat.Appendable =
    applyView(viewModel, None)

  private def applyView(
    viewModel: ReviewDepartureErrorsP5ViewModel,
    mrn: Option[String]
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewDepartureErrorsP5View]
      .apply(viewModel, departureId.toString, mrn)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(viewModel.caption)

  behave like pageWithPagination()

  behave like pageWithTable()

  behave like pageWithSpecificContent("paragraph-1", viewModel.paragraph1)

  behave like pageWithLink(
    "helpdesk-link",
    viewModel.paragraph2,
    frontendAppConfig.nctsEnquiriesUrl
  )

  "when hyperlink is not defined" - {
    "must not render link" in {
      val doc: Document = parseView(applyView(viewModel.copy(hyperlink = None), None))
      assertNotRenderedById(doc, "departure-link")
    }
  }

  "when hyperlink is defined" - {
    val hyperlink     = nonEmptyString.sample.value
    val doc: Document = parseView(applyView(viewModel.copy(hyperlink = Some(hyperlink)), None))
    behave like pageWithLink(
      doc,
      "departure-link",
      hyperlink,
      frontendAppConfig.p5Departure
    )
  }

  "when MRN is undefined" - {
    "must not render MRN" in {
      val doc: Document = parseView(applyView(viewModel, None))
      assertNotRenderedById(doc, "mrn")
    }
  }

  "when MRN is defined" - {
    "must render MRN" in {
      val mrn           = nonEmptyString.sample.value
      val doc: Document = parseView(applyView(viewModel, Some(mrn)))
      assertRenderedById(doc, "mrn")
    }
  }
}
