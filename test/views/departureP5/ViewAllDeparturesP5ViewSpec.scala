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

import forms.DeparturesSearchFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.departureP5.ViewAllDeparturesP5View

class ViewAllDeparturesP5ViewSpec
    extends MovementsTableViewBehaviours[ViewDepartureP5]
    with SearchViewBehaviours
    with PaginationViewBehaviours[ViewDepartureP5, ViewAllDepartureMovementsP5ViewModel] {

  override def form: Form[String] = new DeparturesSearchFormProvider()()

  override val viewModel: ViewAllDepartureMovementsP5ViewModel =
    arbitraryViewAllDepartureMovementsP5ViewModel.arbitrary.sample.value

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): ViewAllDepartureMovementsP5ViewModel =
    viewModel.copy(
      items = {
        def departure: ViewDepartureP5 = arbitrary[ViewDepartureP5].sample.value
        Seq.fill(totalNumberOfItems)(departure)
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage,
      totalNumberOfItems = totalNumberOfItems
    )

  override val prefix: String = "viewDepartureDeclarationsP5"

  override val movementsPerPage: Int = paginationAppConfig.numberOfMovements

  override val viewMovements: Seq[ViewDepartureP5] = viewModel.items

  override def viewWithSpecificPagination(viewModel: ViewAllDepartureMovementsP5ViewModel): HtmlFormat.Appendable =
    viewWithSpecificPagination(form, viewModel)

  override def viewWithSpecificSearchResults(numberOfSearchResults: Int, searchParam: String): HtmlFormat.Appendable =
    viewWithSpecificPagination(
      form.fill(searchParam),
      viewModel.copy(
        items = {
          def departure: ViewDepartureP5 = arbitrary[ViewDepartureP5].sample.value
          Seq.fill(numberOfSearchResults)(departure)
        },
        searchParam = Some(searchParam),
        totalNumberOfItems = numberOfSearchResults
      )
    )

  private def viewWithSpecificPagination(
    form: Form[String],
    viewModel: ViewAllDepartureMovementsP5ViewModel
  ): HtmlFormat.Appendable =
    applyView(form, viewModel)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(form, viewModel)

  private def applyView(
    form: Form[String],
    viewModel: ViewAllDepartureMovementsP5ViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllDeparturesP5View]
      .apply(form, viewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithInsetText("Reload this page for the latest status updates.")

  behave like pageWithLink(
    id = "make-departure-declaration",
    expectedText = "Make a departure declaration",
    expectedHref = frontendAppConfig.p5Departure
  )

  behave like pageWithSearch(
    "Search by Local Reference Number (LRN)",
    "No results found"
  )

  behave like pageWithPagination()

  behave like pageWithMovementsData("Local Reference Number (LRN)")

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Manage your transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )
}
