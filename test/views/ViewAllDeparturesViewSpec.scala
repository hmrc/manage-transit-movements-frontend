/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.behaviours.MovementsTableViewBehaviours
import views.html.ViewAllDeparturesView

class ViewAllDeparturesViewSpec extends MovementsTableViewBehaviours[ViewDeparture] with Generators with ScalaCheckPropertyChecks {

  override val prefix: String = "viewDepartures"

  override val referenceNumberType: String = "lrn"

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val viewAllDepartureMovementsViewModel = arbitrary[ViewAllDepartureMovementsViewModel].sample.value

  override val viewMovements: Seq[ViewDeparture] = viewAllDepartureMovementsViewModel.dataRows.flatMap(_._2)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[ViewAllDeparturesView].apply(viewAllDepartureMovementsViewModel)(fakeRequest, messages)

  override def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllDeparturesView]
      .apply(ViewAllDepartureMovementsViewModel(Seq.empty[ViewDeparture], paginationViewModel))(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "make-departure-notification",
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
