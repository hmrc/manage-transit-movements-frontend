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

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel
import views.behaviours.DetailsListViewBehaviours
import views.html.departureP5.IncidentsDuringTransitP5View

class IncidentsDuringTransitP5ViewSpec extends DetailsListViewBehaviours with Generators {

  private val isMultipleIncidents = arbitrary[Boolean].sample.value

  override val prefix: String = if (isMultipleIncidents) "arrival.notification.incidents" else "arrival.notification.incident"

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[IncidentsDuringTransitP5View].apply(viewModel(isMultipleIncidents))(fakeRequest, messages)

  private def viewModel(isMultipleIncidents: Boolean): IncidentsDuringTransitP5ViewModel =
    new IncidentsDuringTransitP5ViewModel(lrn.toString, Left("customId"), isMultipleIncidents, sections)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"LRN: ${lrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    if (isMultipleIncidents) {
      "Multiple incidents have been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
    } else {
      "An incident has been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
    }
  )

  behave like pageWithContent("p", "Check your departure declarations for further updates.")

  behave like pageWithLink(
    "departures-link",
    "Check your departure declarations",
    controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
  )

  behave like pageWithSections()

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }
}
