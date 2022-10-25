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

package views.departure

import models.FunctionalError
import play.twirl.api.HtmlFormat
import views.behaviours.NegativeAcknowledgementViewBehaviours
import views.html.departure.DepartureXmlNegativeAcknowledgementView

class DepartureXmlNegativeAcknowledgementViewSpec extends NegativeAcknowledgementViewBehaviours {

  override def applyView(functionalError: FunctionalError): HtmlFormat.Appendable =
    injector.instanceOf[DepartureXmlNegativeAcknowledgementView].apply(functionalError)(fakeRequest, messages)

  override val prefix: String = "departureXmlNegativeAcknowledgement"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithPartialContent("p", "You must")
  behave like pageWithLink(
    id = "contact",
    expectedText = "contact the New Computerised Transit System helpdesk (opens in a new tab)",
    expectedHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"
  )
}
