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

import generators.Generators
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departure.TestOnly.GoodsNotReleasedP5View

class GoodsNotReleasedP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "departure.notReleased"

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[GoodsNotReleasedP5View]
      .apply(GoodsNotReleasedP5ViewModel(sections, "AB123"))(fakeRequest, messages, frontendAppConfig)

  behave like pageWithMatchingTitleAndHeading()

  behave like pageWithBackLink()

  behave like pageWithContent("p",
                              "Customs have reviewed this declaration and decided not to release the goods for transit. This means the movement has now ended."
  )

  behave like pageWithLink(
    id = "contact-helpdesk",
    expectedText = "New Computerised Transit System helpdesk",
    expectedHref = frontendAppConfig.nctsHelpdeskUrl
  )
}
