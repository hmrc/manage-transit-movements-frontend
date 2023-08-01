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

package views.departure

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.pagination.ListPaginationViewModel
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.departure.ViewAllDeparturesView

class ViewAllDeparturesViewSpec
    extends MovementsTableViewBehaviours[ViewDeparture]
    with SearchViewBehaviours[ViewDeparture]
    with PaginationViewBehaviours[ListPaginationViewModel]
    with Generators
    with ScalaCheckPropertyChecks {

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  override val prefix: String = "viewDepartures"

  override val referenceNumberType: String = "lrn"

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val viewAllDepartureMovementsViewModel = arbitrary[ViewAllDepartureMovementsViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewDeparture])] = viewAllDepartureMovementsViewModel.dataRows

  override val viewMovements: Seq[ViewDeparture] = dataRows.flatMap(_._2)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(form, ViewAllDepartureMovementsViewModel(Seq.empty[ViewDeparture], paginationViewModel))

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, viewAllDepartureMovementsViewModel)

  private def applyView(
    form: Form[String],
    viewAllDepartureMovementsViewModel: ViewAllDepartureMovementsViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllDeparturesView]
      .apply(form, viewAllDepartureMovementsViewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "make-departure-declaration",
    expectedText = "Make a departure declaration",
    expectedHref = frontendAppConfig.declareDepartureStartWithLRNUrl
  )

  behave like pageWithMovementSearch("Search by local reference number")

  behave like pageWithPagination(controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)

  behave like pageWithMovementsData()

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Go to manage transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )
}
