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

package components

import a11ySpecBase.A11ySpecBase
import viewModels.P5.arrival.ViewArrivalP5
import viewModels.P5.departure.ViewDepartureP5
import views.html.components.MovementsTable
import views.html.templates.MainTemplate

class MovementsTableSpec extends A11ySpecBase {

  "the 'movements table' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[MovementsTable]

    val title                     = nonEmptyString.sample.value
    val visuallyHiddenHeader      = nonEmptyString.sample.value
    val rowHeadingUpdated         = nonEmptyString.sample.value
    val rowHeadingReferenceNumber = nonEmptyString.sample.value
    val rowHeadingStatus          = nonEmptyString.sample.value
    val rowHeadingAction          = nonEmptyString.sample.value

    "pass accessibility checks" when {

      "departure movements" in {
        val departures = listWithMaxLength[ViewDepartureP5]().sample.value
        val content = template.apply(title) {
          component.apply(departures, visuallyHiddenHeader, rowHeadingUpdated, rowHeadingReferenceNumber, rowHeadingStatus, rowHeadingAction).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }

      "arrival movements" in {
        val arrivals = listWithMaxLength[ViewArrivalP5]().sample.value
        val content = template.apply(title) {
          component.apply(arrivals, visuallyHiddenHeader, rowHeadingUpdated, rowHeadingReferenceNumber, rowHeadingStatus, rowHeadingAction).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }

}
