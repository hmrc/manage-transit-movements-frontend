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
import viewModels.P5.{ViewAllArrivalMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.MovementsPaginationViewModel
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.arrival.P5.{ViewAllArrivalsP5View, ViewAllDeparturesP5View}

class ViewAllDeparturesP5ViewSpec
    extends MovementsTableViewBehaviours[ViewDepartureP5]
    with SearchViewBehaviours[ViewDepartureP5]
    with PaginationViewBehaviours[MovementsPaginationViewModel]
    with Generators
    with ScalaCheckPropertyChecks {

  override val prefix: String = "viewDepartureDeclarationsP5"

  override val referenceNumberType: String = "lrn"

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val viewAllArrivalMovementsP5ViewModel = arbitrary[ViewAllArrivalMovementsP5ViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewDepartureP5])] = viewAllArrivalMovementsP5ViewModel.dataRows

  override val viewMovements: Seq[ViewDepartureP5] = dataRows.flatMap(_._2)

  override def viewWithSpecificPagination(paginationViewModel: MovementsPaginationViewModel): HtmlFormat.Appendable =
    applyView(form, ViewAllArrivalMovementsP5ViewModel(Seq.empty[ViewDepartureP5], paginationViewModel))

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, viewAllArrivalMovementsP5ViewModel)

  override val buildViewModel: (Int, Int, Int, String) => MovementsPaginationViewModel = MovementsPaginationViewModel(_, _, _, _)

  private def applyView(
    form: Form[String],
    viewAllArrivalMovementsViewModel: ViewAllArrivalMovementsP5ViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllDeparturesP5View]
      .apply(form)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "make-departure-declaration",
    expectedText = "Create a departure declaration",
    expectedHref = frontendAppConfig.startDepartureDeclarationStartUrl
  )

  behave like pageWithMovementSearch("Search by Movement Reference Number (MRN)")

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Manage your transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )
}
