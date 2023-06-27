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

package views.departure.testOnly

import play.twirl.api.HtmlFormat
import viewModels.P5.departure.CancellationNotificationErrorsP5ViewModel
import views.behaviours.ViewBehaviours
import views.html.departure.TestOnly.CancellationNotificationErrorsP5View

class CancellationNotificationErrorsP5ViewSpec extends ViewBehaviours {

  private val cancellationNotificationErrorsP5ViewViewModel =
    new CancellationNotificationErrorsP5ViewModel("AB123", true, "CD123", None)

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[CancellationNotificationErrorsP5View]
      .apply(cancellationNotificationErrorsP5ViewViewModel)(fakeRequest, messages)

  override val prefix: String = "cancellation.notification.errors.message"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", "There are one or more errors with the cancellation of departure declaration AB123.")

  behave like pageWithContent("p", "Try cancelling the declaration again. Or for more information, contact Customs office CD123.")

  behave like pageWithLink(
    id = "view-departure-declaration",
    expectedText = "View departure declarations",
    expectedHref = controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
  )
}
