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

package views

import generators.Generators
import models.Availability
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.WhatDoYouWantToDoView

class WhatDoYouWantToDoViewSpec extends ViewBehaviours with Generators {

  private val sampleAvailability = arbitrary[Availability].sample.value

  private def applyView(
    arrivalsAvailability: Availability = sampleAvailability,
    departuresAvailability: Availability = sampleAvailability
  ): HtmlFormat.Appendable =
    injector.instanceOf[WhatDoYouWantToDoView].apply(arrivalsAvailability, departuresAvailability)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView()

  override val prefix: String = "whatDoYouWantToDo"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("h2", "Arrivals")
  behave like pageWithLink(
    "make-arrival-notification",
    "Make an arrival notification",
    "http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number"
  )

  behave like pageWithContent("h2", "Departures")
  behave like pageWithLink(
    "make-departure-declaration",
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
    val doc = parseView(applyView(arrivalsAvailability = Availability.Empty))
    behave like pageWithContent(doc, "p", "You have no arrival notifications.")
  }

  "when arrivals are unavailable" - {
    val doc = parseView(applyView(arrivalsAvailability = Availability.Unavailable))
    behave like pageWithContent(doc, "p", "View arrival notifications is currently unavailable.")
  }

  "when we have arrivals must" - {
    val doc  = parseView(applyView(arrivalsAvailability = Availability.NonEmpty))
    val link = getElementById(doc, "view-arrival-notifications")

    "have the correct text for the view arrivals link" in {
      assertElementContainsText(link, "View arrival notifications")
    }

    "have the correct href on the view arrivals link" in {
      assertElementContainsHref(link, "/manage-transit-movements/view-arrivals")
    }
  }

  "when we have no departures" - {
    val doc = parseView(applyView(departuresAvailability = Availability.Empty))
    behave like pageWithContent(doc, "p", "You have no departure declarations.")
  }

  "when departures are unavailable" - {
    val doc = parseView(applyView(departuresAvailability = Availability.Unavailable))
    behave like pageWithContent(doc, "p", "View departure notifications is currently unavailable.")
  }

  "when we have departures must" - {
    val doc  = parseView(applyView(departuresAvailability = Availability.NonEmpty))
    val link = getElementById(doc, "view-departure-declarations")

    "have the correct text for the view departures link" in {
      assertElementContainsText(link, "View departure declarations")
    }

    "have the correct href on the view departures link" in {
      assertElementContainsHref(link, "/manage-transit-movements/view-departures")
    }
  }
}
