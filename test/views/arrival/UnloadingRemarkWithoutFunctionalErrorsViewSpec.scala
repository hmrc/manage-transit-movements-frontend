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

package views.arrival

import play.twirl.api.HtmlFormat
import viewModels.arrival.UnloadingRemarkWithoutFunctionalErrorsViewModel
import views.behaviours.ViewBehaviours
import views.html.arrival.UnloadingRemarkWithoutFunctionalErrorsView

class UnloadingRemarkWithoutFunctionalErrorsViewSpec extends ViewBehaviours {

  private val unloadingNotificationErrorsViewModel =
    new UnloadingRemarkWithoutFunctionalErrorsViewModel("AB123", fakeCustomsOffice)

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingRemarkWithoutFunctionalErrorsView]
      .apply(unloadingNotificationErrorsViewModel, arrivalIdP5, messageId)(fakeRequest, messages)

  override val prefix: String = "arrival.notification.unloading.errors.message"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption("MRN: AB123")

  behave like pageWithContent("p", "There are one or more errors with the unloading remarks for this notification.")

  behave like pageWithSubmitButton("Make unloading remarks")

  behave like pageWithLink(
    id = "arrival-link",
    expectedText = "View arrival notifications",
    expectedHref = controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None, None).url
  )

}
