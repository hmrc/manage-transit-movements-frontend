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

package views.arrival.P5

import play.twirl.api.HtmlFormat
import viewModels.P5.arrival.UnloadingRemarkWithoutFunctionalErrorsP5ViewModel
import views.behaviours.ViewBehaviours
import views.html.arrival.P5.UnloadingRemarkWithoutFunctionalErrorsP5View

class UnloadingRemarkWithoutFunctionalErrorsP5ViewSpec extends ViewBehaviours {

  private val unloadingNotificationErrorsP5ViewModel =
    new UnloadingRemarkWithoutFunctionalErrorsP5ViewModel("AB123", "CD123", None)

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingRemarkWithoutFunctionalErrorsP5View]
      .apply(unloadingNotificationErrorsP5ViewModel)(fakeRequest, messages)

  override val prefix: String = "arrival.notification.unloading.errors.message"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption("MRN: AB123")

  behave like pageWithContent("p", "There are one or more errors with the unloading remarks for this notification.")

  behave like pageWithContent("p", "Try making the unloading remarks again. Or for more information, contact Customs office CD123.")

  behave like pageWithLink(
    id = "arrival-link",
    expectedText = "View arrival notifications",
    expectedHref = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
  )
}
