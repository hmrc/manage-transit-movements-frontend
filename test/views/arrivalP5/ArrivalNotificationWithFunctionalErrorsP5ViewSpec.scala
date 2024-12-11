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
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.arrivalP5.ArrivalNotificationWithFunctionalErrorsP5View

class ArrivalNotificationWithFunctionalErrorsP5ViewSpec
    extends PaginationViewBehaviours[FunctionalErrorWithoutSection, ArrivalNotificationWithFunctionalErrorsP5ViewModel]
    with TableViewBehaviours
    with Generators {

  override val viewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel =
    arbitraryArrivalNotificationWithFunctionalErrorsP5ViewModel.arbitrary.sample.value

  override val table: Table = viewModel.table

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): ArrivalNotificationWithFunctionalErrorsP5ViewModel =
    viewModel.copy(
      functionalErrors = {
        def error: FunctionalErrorWithoutSection = arbitrary[FunctionalErrorWithoutSection].sample.value
        FunctionalErrorsWithoutSection(Seq.fill(totalNumberOfItems)(error))
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage
    )

  override val prefix: String        = "arrival.ie057.review.notification.message"
  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfErrorsPerPage

  override def view: HtmlFormat.Appendable = applyView(viewModel)

  override def viewWithSpecificPagination(viewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel): HtmlFormat.Appendable =
    applyView(viewModel)

  private def applyView(
    viewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationWithFunctionalErrorsP5View]
      .apply(viewModel, arrivalIdP5)(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(viewModel.caption)

  behave like pageWithPagination()

  behave like pageWithTable()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  behave like pageWithSpecificContent("paragraph-1", viewModel.paragraph1)

  behave like pageWithSpecificContent("paragraph-2", viewModel.paragraph2)

  behave like pageWithLink(
    "helpdesk-link",
    viewModel.paragraph3,
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "arrival-link",
    viewModel.hyperlink,
    frontendAppConfig.p5Arrival
  )
}
