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

package views.departure.drafts

import generators.Generators
import models.DraftDeparture
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.behaviours.{DraftDepartureTableViewBehaviours, MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.departure.ViewAllDeparturesView
import views.html.departure.drafts.DashboardView

class DashboardViewSpec
    extends Generators
    with ScalaCheckPropertyChecks {

  val prefix: String = "viewDraftDepartures"

  private val viewAllDraftDeparturesViewModel = arbitrary[AllDraftDeparturesViewModel].sample.value
//
//  val dataRows: Seq[DraftDepartureM] = viewAllDraftDeparturesViewModel.dataRows
//
//  val draftDeparture: Seq[DraftDeparture] = dataRows.

//  def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, viewAllDepartureMovementsViewModel)

  private def applyView(
    form: Form[String],
    viewAllDepartureMovementsViewModel: ViewAllDepartureMovementsViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[DashboardView]
      .apply(form, viewAllDepartureMovementsViewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithMovementsData()


}
