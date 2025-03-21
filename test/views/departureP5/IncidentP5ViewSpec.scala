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
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.IncidentP5ViewModel
import views.behaviours.DetailsListViewBehaviours
import views.html.departureP5.IncidentP5View

class IncidentP5ViewSpec extends DetailsListViewBehaviours with Generators {

  override val prefix: String = "departure.notification.incident.index"

  def applyView(
    isMultipleIncidents: Boolean
  ): HtmlFormat.Appendable =
    injector.instanceOf[IncidentP5View].apply(viewModel(isMultipleIncidents), departureId.toString, messageId)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[IncidentP5View].apply(viewModel(false), departureId.toString, messageId)(fakeRequest, messages)

  private def viewModel(isMultipleIncidents: Boolean): IncidentP5ViewModel =
    new IncidentP5ViewModel(lrn.toString, fakeCustomsOffice, isMultipleIncidents, sections, incidentIndex)

  behave like pageWithTitle(incidentIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"LRN: ${lrn.toString}")

  behave like pageWithHeading(incidentIndex.display)

  behave like pageWithSubmitButton("Back to summary")

  behave like pageWithContent(
    "p",
    "An incident has been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
  )

  behave like pageWithSections()

  "when isMultipleIncidents is true" - {
    val doc = parseView(applyView(isMultipleIncidents = true))
    "must render correct paragraph" - {
      behave like pageWithContent(
        doc,
        "p",
        "Multiple incidents have been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
      )
    }
  }

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("span", sectionTitle)
    })
  }

}
