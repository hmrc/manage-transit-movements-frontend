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

package views.arrival

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.behaviours.{MovementsTableViewBehaviours, SearchViewBehaviours}
import views.html.arrival.ViewArrivalsSearchResultsView

class ViewArrivalsSearchResultsViewSpec
    extends MovementsTableViewBehaviours[ViewArrival]
    with SearchViewBehaviours[ViewArrival]
    with Generators
    with ScalaCheckPropertyChecks {

  override val prefix: String = "viewArrivalNotifications"

  override val referenceNumberType: String = "mrn"

  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfMovements

  private val viewAllArrivalMovementsViewModel = arbitrary[ViewAllArrivalMovementsViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewArrival])] = viewAllArrivalMovementsViewModel.dataRows

  override val viewMovements: Seq[ViewArrival] = dataRows.flatMap(_._2)

  private val retrieved: Int = arbitrary[Int].sample.value

  private val tooManyResults: Boolean = arbitrary[Boolean].sample.value

  override def viewWithSpecificSearchResults(
    dataRows: Seq[(String, Seq[ViewArrival])],
    retrieved: Int,
    tooManyResults: Boolean
  ): HtmlFormat.Appendable = applyView(form, dataRows, retrieved, tooManyResults)

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, dataRows, retrieved, tooManyResults)

  private def applyView(
    form: Form[String],
    dataRows: Seq[(String, Seq[ViewArrival])],
    retrieved: Int,
    tooManyResults: Boolean
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewArrivalsSearchResultsView]
      .apply(form, mrn, dataRows, retrieved, tooManyResults)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "go-to-view-all-movements",
    expectedText = "View all movements",
    expectedHref = controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
  )

  behave like pageWithMovementSearch("Search by movement reference number")

  behave like pageWithSearchResults(mrn)

  behave like pageWithMovementsData()

}
