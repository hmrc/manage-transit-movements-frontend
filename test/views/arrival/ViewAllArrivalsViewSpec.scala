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

package views.arrival

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.arrival.ViewAllArrivalsView

class ViewAllArrivalsViewSpec
    extends MovementsTableViewBehaviours[ViewArrival]
    with SearchViewBehaviours[ViewArrival]
    with PaginationViewBehaviours[ViewArrival]
    with Generators
    with ScalaCheckPropertyChecks {

  override val prefix: String = "viewArrivalNotifications"

  override val referenceNumberType: String = "mrn"

  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfMovements

  private val viewAllArrivalMovementsViewModel = arbitrary[ViewAllArrivalMovementsViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewArrival])] = viewAllArrivalMovementsViewModel.dataRows

  override val viewMovements: Seq[ViewArrival] = dataRows.flatMap(_._2)

  override def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable =
    applyView(form, ViewAllArrivalMovementsViewModel(Seq.empty[ViewArrival], paginationViewModel))

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, viewAllArrivalMovementsViewModel)

  private def applyView(
    form: Form[String],
    viewAllArrivalMovementsViewModel: ViewAllArrivalMovementsViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllArrivalsView]
      .apply(form, viewAllArrivalMovementsViewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "make-arrival-notification",
    expectedText = "Make an arrival notification",
    expectedHref = frontendAppConfig.declareArrivalNotificationStartUrl
  )

  behave like pageWithMovementSearch("Search by movement reference number")

  behave like pageWithPagination(controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)

  behave like pageWithMovementsData()

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Go to manage transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )

}
