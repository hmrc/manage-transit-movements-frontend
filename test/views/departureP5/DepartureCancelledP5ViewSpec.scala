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
import viewModels.P5.departure.DepartureCancelledP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.DepartureCancelledP5View

class DepartureCancelledP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "departure.cancellation.message"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[DepartureCancelledP5View]
      .apply(DepartureCancelledP5ViewModel(sections, "AB123", "CD123", None, isCancelled = true))(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", "The office of departure cancelled the declaration for LRN AB123 as requested.")

  behave like pageWithContent("p", "If you have any questions, contact Customs office CD123.")

  behave like pageWithLink(
    id = "link",
    expectedText = "Make another departure declaration",
    expectedHref = frontendAppConfig.declareDepartureStartWithLRNUrl
  )

  "when isCancelled is false" - {

    val document = parseView(
      injector
        .instanceOf[DepartureCancelledP5View]
        .apply(DepartureCancelledP5ViewModel(sections, "AB123", "CD123", None, isCancelled = false))(fakeRequest, messages, frontendAppConfig)
    )

    val prefixNotCancelled = "departure.cancellation.notCancelled.message"

    behave like pageWithTitle(document, prefix = prefixNotCancelled)

    behave like pageWithHeading(document, prefix = prefixNotCancelled)

    behave like pageWithContent(doc = document, "p", "The office of departure could not cancel the declaration for LRN AB123 as requested.")

    behave like pageWithContent(doc = document, "p", "If you have any questions, contact Customs office CD123.")

  }
}
