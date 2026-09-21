/*
 * Copyright 2024 HM Revenue & Customs
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

package views.arrival

import forms.DeparturesSearchFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.arrival.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.arrival.ViewAllArrivalsView

class ViewAllArrivalsViewSpec
    extends MovementsTableViewBehaviours[ViewArrival]
    with SearchViewBehaviours
    with PaginationViewBehaviours[ViewArrival, ViewAllArrivalMovementsViewModel] {

  override def form: Form[Option[String]] = new DeparturesSearchFormProvider()()

  override val viewModel: ViewAllArrivalMovementsViewModel =
    arbitraryViewAllArrivalMovementsViewModel.arbitrary.sample.value

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): ViewAllArrivalMovementsViewModel =
    viewModel.copy(
      items = {
        def arrival: ViewArrival = arbitrary[ViewArrival].sample.value
        Seq.fill(totalNumberOfItems)(arrival)
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage,
      totalNumberOfItems = totalNumberOfItems
    )

  override val prefix: String = "viewArrivalNotifications"

  override val movementsPerPage: Int = paginationAppConfig.numberOfMovements

  override val viewMovements: Seq[ViewArrival] = viewModel.items

  override def viewWithSpecificPagination(viewModel: ViewAllArrivalMovementsViewModel): HtmlFormat.Appendable =
    viewWithSpecificPagination(form, viewModel)

  override def viewWithSpecificSearchResults(
    numberOfSearchResults: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int,
    searchParam: Option[String]
  ): HtmlFormat.Appendable =
    viewWithSpecificPagination(
      form.fill(searchParam),
      viewModel.copy(
        items = {
          def arrival: ViewArrival = arbitrary[ViewArrival].sample.value
          Seq.fill(numberOfSearchResults)(arrival)
        },
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage,
        searchParam = searchParam,
        totalNumberOfItems = numberOfSearchResults
      )
    )

  private def viewWithSpecificPagination(
    form: Form[Option[String]],
    viewModel: ViewAllArrivalMovementsViewModel
  ): HtmlFormat.Appendable =
    applyView(form, viewModel)

  override def applyView(form: Form[Option[String]]): HtmlFormat.Appendable =
    applyView(form, viewModel)

  private def applyView(
    form: Form[Option[String]],
    viewModel: ViewAllArrivalMovementsViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllArrivalsView]
      .apply(form, viewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithInsetText("Reload this page for the latest status updates.")

  behave like pageWithLink(
    id = "make-arrival-notification",
    expectedText = "Make an arrival notification",
    expectedHref = frontendAppConfig.arrival
  )

  behave like pageWithSearch(
    "Search by Movement Reference Number (MRN)",
    "No results found",
    viewModel.numberOfItemsPerPage
  )

  behave like pageWithPagination()

  behave like pageWithMovementsData("Movement Reference Number (MRN)")

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Manage your transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )
}
