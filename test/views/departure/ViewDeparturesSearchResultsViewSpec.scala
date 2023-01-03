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
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.behaviours.{MovementsTableViewBehaviours, SearchViewBehaviours}
import views.html.departure.ViewDeparturesSearchResultsView

class ViewDeparturesSearchResultsViewSpec
    extends MovementsTableViewBehaviours[ViewDeparture]
    with SearchViewBehaviours[ViewDeparture]
    with Generators
    with ScalaCheckPropertyChecks {

  override val prefix: String = "viewDepartures"

  override val referenceNumberType: String = "lrn"

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val viewAllDepartureMovementsViewModel = arbitrary[ViewAllDepartureMovementsViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewDeparture])] = viewAllDepartureMovementsViewModel.dataRows

  override val viewMovements: Seq[ViewDeparture] = dataRows.flatMap(_._2)

  private val retrieved: Int = arbitrary[Int].sample.value

  private val tooManyResults: Boolean = arbitrary[Boolean].sample.value

  override def viewWithSpecificSearchResults(
    dataRows: Seq[(String, Seq[ViewDeparture])],
    retrieved: Int,
    tooManyResults: Boolean
  ): HtmlFormat.Appendable = applyView(form, dataRows, retrieved, tooManyResults)

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, dataRows, retrieved, tooManyResults)

  private def applyView(
    form: Form[String],
    dataRows: Seq[(String, Seq[ViewDeparture])],
    retrieved: Int,
    tooManyResults: Boolean
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewDeparturesSearchResultsView]
      .apply(form, lrn.toString, dataRows, retrieved, tooManyResults)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "go-to-view-all-movements",
    expectedText = "View all movements",
    expectedHref = controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url
  )

  behave like pageWithMovementSearch("Search by local reference number")

  behave like pageWithSearchResults(lrn.toString)

  behave like pageWithMovementsData()

}
