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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.departureP5.PreLodgedDeclarationErrorsView

class PreLodgedDeclarationErrorsViewSpec extends ViewBehaviours {

  override val prefix: String = "departure.prelodged.errors"

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[PreLodgedDeclarationErrorsView].apply(departureIdP5, lrn.value)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    "There are one or more errors in this declaration that cannot be amended. Complete your pre-lodged declaration with the right information."
  )

  behave like pageWithLink(
    id = "helpdesk-link",
    expectedText = "Contact the New Computerised Transit System helpdesk for help understanding errors (opens in a new tab)",
    expectedHref = frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    id = "complete-declaration",
    expectedText = "Complete pre-lodged declaration",
    expectedHref = frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)
  )

}
