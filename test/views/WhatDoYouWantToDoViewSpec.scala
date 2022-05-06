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
    injector.instanceOf[WhatDoYouWantToDoView].apply(Availability.NonEmpty, Availability.NonEmpty)(fakeRequest, messages)

  override val prefix: String = "whatDoYouWantToDo"

  private val contactUrl = "https://www.gov.uk/new-computerised-transit-system"

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
}
