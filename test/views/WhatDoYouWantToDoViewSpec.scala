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

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.Empty, Availability.Empty)(fakeRequest, messages)

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
  behave like pageWithContent("p", "You have no arrival notifications.")

  val viewWithArrivalsUnavailable = injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.Unavailable, Availability.Empty)(fakeRequest, messages)
  val docWithArrivalsUnavailable  = parseView(viewWithArrivalsUnavailable)
  behave like pageWithContent(docWithArrivalsUnavailable, "p", "View arrival notifications is currently unavailable.")

  val viewWithArrivals = injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.NonEmpty, Availability.Empty)(fakeRequest, messages)
  val docWithArrivals  = parseView(viewWithArrivals)
  "must render link when we have arrivals " in {
    val link = getElementById(docWithArrivals, "view-arrival-notifications")
    assertElementContainsText(link, "View arrival notifications")
    assertElementContainsHref(link, "/manage-transit-movements/view-arrivals")
  }

  behave like pageWithContent("h2", "Departures")
  behave like pageWithLink(
    "make-departure-notification",
    "Make a departure declaration",
    "http://localhost:9489/manage-transit-movements-departures/local-reference-number"
  )
  behave like pageWithContent("p", "You have no departure declarations.")

  val viewWithDeparturesUnavailable = injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.Empty, Availability.Unavailable)(fakeRequest, messages)
  val docWithDeparturesUnavailable  = parseView(viewWithDeparturesUnavailable)
  behave like pageWithContent(docWithDeparturesUnavailable, "p", "View departure notifications is currently unavailable.")

  val viewWithDepartures = injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.Empty, Availability.NonEmpty)(fakeRequest, messages)
  val docWithDepartures  = parseView(viewWithDepartures)
  "must render link when we have departures " in {
    val link = getElementById(docWithDepartures, "view-departure-declarations")
    assertElementContainsText(link, "View departure declarations")
    assertElementContainsHref(link, "/manage-transit-movements/view-departures")
  }

  behave like pageWithContent("h2", "Guarantees")
  behave like pageWithLink(
    "check-guarantee-balance",
    "Check your guarantee balance",
    "http://localhost:9462/check-transit-guarantee-balance/start?referral=ncts"
  )

}
