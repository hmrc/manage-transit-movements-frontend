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
import viewModels.P5.departure.DepartureNotCancelledP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.DepartureNotCancelledP5View

class DepartureNotCancelledP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "departure.notCancelled"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[DepartureNotCancelledP5View]
      .apply(DepartureNotCancelledP5ViewModel(sections, departureIdP5, "AB123"))(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "try-again",
    expectedText = "Try cancelling declaration again",
    expectedHref = s"http://localhost:10122/manage-transit-movements/cancellation/$departureIdP5/index/AB123"
  )

  behave like pageWithLink(
    id = "helpdesk-link",
    expectedText = "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)",
    expectedHref = frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    id = "link",
    expectedText = "Make another departure declaration",
    expectedHref = frontendAppConfig.p5Departure
  )

}
