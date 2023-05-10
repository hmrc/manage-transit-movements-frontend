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

package views.departure.drafts

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.departure.drafts.DraftLockedView

class DraftLockedViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable = applyView()

  private def applyView(): HtmlFormat.Appendable =
    injector.instanceOf[DraftLockedView].apply()(fakeRequest, messages)

  override val prefix: String = "departure.drafts.dashboard.draftLocked"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p",
                              "Another person from your organisation is currently working on this declaration. You cannot delete it until they’re finished."
  )

  behave like pageWithButton("Return to drafts") {
    assertElementContainsHref(_, controllers.departure.drafts.routes.DashboardController.onPageLoad(None, None, None).url)
  }

}
