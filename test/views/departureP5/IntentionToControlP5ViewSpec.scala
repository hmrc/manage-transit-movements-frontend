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
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.{CustomsOfficeContactViewModel, IntentionToControlP5ViewModel}
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.IntentionToControlP5View

class IntentionToControlP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  private val customsOffice: CustomsOffice   = arbitrary[CustomsOffice].sample.value
  private val customsReferenceNumber: String = Gen.alphaNumStr.sample.value

  override val prefix: String = "departure.ie060.message.prelodged"

  private val intentionToControlP5ViewModel: IntentionToControlP5ViewModel = new IntentionToControlP5ViewModel(sections, false, Some(lrn.toString))
  private val customsOfficeContactViewModel: CustomsOfficeContactViewModel = CustomsOfficeContactViewModel(customsReferenceNumber, Some(customsOffice))

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[IntentionToControlP5View]
      .apply(intentionToControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithMatchingTitleAndHeading()

  behave like pageWithBackLink()

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

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render subheading" in {
    assertSpecificElementContainsText("subheading", "What happens next")
  }

  "must render correct paragraph content" in {
    assertSpecificElementContainsText(
      "paragraph1",
      "Customs are intending to place this declaration under control while they carry out further checks. This is because of a possible risk to health and safety."
    )
    assertSpecificElementContainsText(
      "paragraph2",
      "While under control, the goods will remain under supervision at the office of destination."
    )
    assertSpecificElementContainsText(
      "paragraph3",
      "Once Customs have completed their checks, they will notify you with the outcome."
    )
  }

  "must render correct link text" in {
    assertSpecificElementContainsText("link-text", "You must wait for the outcome of Customs’ checks. Check your departure declarations for further updates.")
  }

  "must not render what happens next section" in {
    assertNotRenderedById(doc, "what-happens-next")
  }
}
