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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.RecoveryNotificationViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.RecoveryNotificationView

class RecoveryNotificationViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "departure.ie035.message"

  private val recoveryNotificationViewModel: RecoveryNotificationViewModel = new RecoveryNotificationViewModel(sections)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[RecoveryNotificationView]
      .apply(recoveryNotificationViewModel, lrn.value)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("h2", sectionTitle)
    })
  }

  behave like pageWithSpecificContent(
    "subheading",
    "What happens next"
  )

  behave like pageWithSpecificContent(
    "paragraph1",
    "There was an issue with this movement during its transit. The goods are now being recovered to a customs office by a local authority."
  )

  behave like pageWithSpecificContent(
    "paragraph2",
    "Review the recovery information and wait for the customs office to contact you."
  )

  behave like pageWithSpecificContent(
    "paragraph3",
    "Customs will contact you to discuss the issue further."
  )

}
