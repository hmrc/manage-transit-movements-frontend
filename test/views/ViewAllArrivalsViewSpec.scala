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
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.behaviours.MovementsTableViewBehaviours
import views.html.ViewAllArrivalsView

class ViewAllArrivalsViewSpec extends MovementsTableViewBehaviours[ViewArrival] with Generators with ScalaCheckPropertyChecks {

  override val prefix: String = "viewArrivalNotifications"

  override val referenceNumberType: String = "mrn"

  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfMovements

  private val viewAllArrivalMovementsViewModel = arbitrary[ViewAllArrivalMovementsViewModel].sample.value

  override val viewMovements: Seq[ViewArrival] = viewAllArrivalMovementsViewModel.dataRows.flatMap(_._2)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[ViewAllArrivalsView].apply(viewAllArrivalMovementsViewModel)(fakeRequest, messages)

  override def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllArrivalsView]
      .apply(ViewAllArrivalMovementsViewModel(Seq.empty[ViewArrival], paginationViewModel))(fakeRequest, messages)

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
