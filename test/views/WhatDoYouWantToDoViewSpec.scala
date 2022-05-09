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

import models.Availability
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatDoYouWantToDoView

class WhatDoYouWantToDoViewSpec extends ViewBehaviours {

  private def applyView(arrivalAvailability: Availability, departureAvailability: Availability): HtmlFormat.Appendable =
    injector.instanceOf[WhatDoYouWantToDoView].apply(arrivalAvailability, departureAvailability)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(Availability.Empty, Availability.Empty)

  override val prefix: String = "whatDoYouWantToDo"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithContent("h2", "Arrivals")
  behave like pageWithLink(
    "make-arrival-notification",
    "Make an arrival notification",
    "http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number"
  )

  behave like pageWithContent("h2", "Departures")
  behave like pageWithLink(
    "make-departure-notification",
    "Make a departure declaration",
    "http://localhost:9489/manage-transit-movements-departures/local-reference-number"
  )

  behave like pageWithContent("h2", "Guarantees")
  behave like pageWithLink(
    "check-guarantee-balance",
    "Check your guarantee balance",
    "http://localhost:9462/check-transit-guarantee-balance/start?referral=ncts"
  )

  "when we have no arrivals" - {
    val docWithNoArrivals = parseView(applyView(Availability.Empty, Availability.Empty))
    behave like pageWithContent(docWithNoArrivals, "p", "You have no arrival notifications.")
  }

  "when arrivals are unavailable" - {
    val docWithArrivalsUnavailable = parseView(applyView(Availability.Unavailable, Availability.Empty))
    behave like pageWithContent(docWithArrivalsUnavailable, "p", "View arrival notifications is currently unavailable.")
  }

  "when we have arrivals must" - {
    val docWithArrivals = parseView(applyView(Availability.NonEmpty, Availability.Empty))
    val link            = getElementById(docWithArrivals, "view-arrival-notifications")
    "have the correct text for the view arrivals link" in {
      assertElementContainsText(link, "View arrival notifications")
    }
    "have the correct href on the view arrivals link" in {
      assertElementContainsHref(link, "/manage-transit-movements/view-arrivals")
    }
  }

  "when we have no departures" - {
    val docWithNoDepartures = parseView(applyView(Availability.Empty, Availability.Empty))
    behave like pageWithContent(docWithNoDepartures, "p", "You have no departure declarations.")
  }

  "when departures are unavailable" - {
    val docWithDeparturesUnavailable = parseView(applyView(Availability.Empty, Availability.Unavailable))
    behave like pageWithContent(docWithDeparturesUnavailable, "p", "View departure notifications is currently unavailable.")
  }

  "when we have departures must" - {
    val docWithDepartures = parseView(applyView(Availability.Empty, Availability.NonEmpty))
    val link              = getElementById(docWithDepartures, "view-departure-declarations")
    "have the correct text for the view departures link" in {
      assertElementContainsText(link, "View departure declarations")
    }
    "have the correct href on the view departures link" in {
      assertElementContainsHref(link, "/manage-transit-movements/view-departures")
    }
  }
}
