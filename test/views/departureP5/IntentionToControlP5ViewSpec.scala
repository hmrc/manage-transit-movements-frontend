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
import models.referenceData.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.IntentionToControlP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.IntentionToControlP5View

class IntentionToControlP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  private val customsOffice: CustomsOffice = arbitrary[CustomsOffice].sample.value

  override val prefix: String = "departure.ie060.message.prelodged"

  private val intentionToControlP5ViewModel: IntentionToControlP5ViewModel =
    new IntentionToControlP5ViewModel(sections, Some(lrn.toString), customsOffice)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[IntentionToControlP5View]
      .apply(intentionToControlP5ViewModel, departureIdP5, messageId)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"LRN: $lrn")

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
    "Customs are intending to place this movement under control and have requested you complete your pre-lodged declaration."
  )

  behave like pageWithSpecificContent(
    "paragraph2",
    "While awaiting your response, the goods will remain under supervision at the office of destination."
  )

  behave like pageWithSpecificContent(
    "paragraph3",
    "Once Customs have reviewed the information you provide, they will notify you with the outcome."
  )

  behave like pageWithSubmitButton("Complete pre-lodged declaration")

  "must render what happens next section" in {
    assertRenderedById(doc, "what-happens-next")
  }
}
