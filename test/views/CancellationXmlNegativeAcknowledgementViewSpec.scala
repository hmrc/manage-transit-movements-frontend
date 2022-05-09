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

import models.FunctionalError
import play.twirl.api.HtmlFormat
import views.behaviours.NegativeAcknowledgementViewBehaviours
import views.html.CancellationXmlNegativeAcknowledgementView

class CancellationXmlNegativeAcknowledgementViewSpec extends NegativeAcknowledgementViewBehaviours {

  override def applyView(functionalError: FunctionalError): HtmlFormat.Appendable =
    injector.instanceOf[CancellationXmlNegativeAcknowledgementView].apply(departureId, functionalError)(fakeRequest, messages)

  override val prefix: String = "cancellationXmlNegativeAcknowledgement"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithNegativeAcknowledgement()

  behave like pageWithContent("p", "You must")

  behave like pageWithLink(
    "create-new-unloading-remarks",
    "cancel declaration",
    s"http://localhost:9495/manage-transit-movements-departures-cancel/${departureId.index}/confirm-cancellation"
  )

  behave like pageWithLink(
    "contact",
    "contact the New Computerised Transit System helpdesk (opens in a new tab)",
    "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"
  )
}
