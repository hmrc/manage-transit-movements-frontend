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
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewModels.P5.departure.ReviewPrelodgedDeclarationErrorsP5ViewModel
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.ReviewPrelodgedDeclarationErrorsP5View

class ReviewPrelodgedDeclarationErrorsP5ViewSpec
    extends PaginationViewBehaviours[FunctionalErrorWithoutSection, ReviewPrelodgedDeclarationErrorsP5ViewModel]
    with TableViewBehaviours
    with Generators {

  override val viewModel: ReviewPrelodgedDeclarationErrorsP5ViewModel =
    arbitraryReviewPrelodgedDeclarationErrorsP5ViewModel.arbitrary.sample.value

  override val tables: Seq[Table] = Seq(viewModel.table)

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): ReviewPrelodgedDeclarationErrorsP5ViewModel =
    viewModel.copy(
      functionalErrors = {
        def error: FunctionalErrorWithoutSection = arbitrary[FunctionalErrorWithoutSection].sample.value
        FunctionalErrorsWithoutSection(Seq.fill(totalNumberOfItems)(error))
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage
    )

  override val prefix: String        = "prelodged.declaration.ie056.review.message"
  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfErrorsPerPage

  override def view: HtmlFormat.Appendable = applyView(viewModel)

  override def viewWithSpecificPagination(viewModel: ReviewPrelodgedDeclarationErrorsP5ViewModel): HtmlFormat.Appendable =
    applyView(viewModel)

  private def applyView(
    viewModel: ReviewPrelodgedDeclarationErrorsP5ViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ReviewPrelodgedDeclarationErrorsP5View]
      .apply(viewModel, departureIdP5)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithCaption(viewModel.caption)

  behave like pageWithPagination()

  behave like pageWithTables()

  behave like pageWithSpecificContent("paragraph-1", viewModel.paragraph1)

  behave like pageWithLink(
    "helpdesk-link",
    viewModel.paragraph2,
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "prelodge-declaration-link",
    viewModel.hyperlink,
    frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)
  )
}
