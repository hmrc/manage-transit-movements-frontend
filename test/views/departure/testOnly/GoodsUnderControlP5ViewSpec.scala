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
import models.referenceData.CustomsOffice
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.{CustomsOfficeContactViewModel, GoodsUnderControlP5ViewModel}
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import views.html.departure.TestOnly.GoodsUnderControlP5View

class GoodsUnderControlP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  private val customsOffice: CustomsOffice   = arbitrary[CustomsOffice].sample.value
  private val customsReferenceNumber: String = Gen.alphaNumStr.sample.value

  override val prefix: String = "departure.ie060.message"

  private val goodsUnderControlP5ViewModel: GoodsUnderControlP5ViewModel   = new GoodsUnderControlP5ViewModel(sections, false, Some(lrn.toString))
  private val customsOfficeContactViewModel: CustomsOfficeContactViewModel = CustomsOfficeContactViewModel(customsReferenceNumber, Some(customsOffice))

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[GoodsUnderControlP5View]
      .apply(goodsUnderControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption(s"LRN: $lrn")

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
      "Customs have placed this declaration under control while they carry out further checks. This is because of a possible discrepancy or risk to health and safety."
    )
    assertSpecificElementContainsText(
      "paragraph2",
      "While under control, the goods will remain under supervision at the office of destination."
    )
    assertSpecificElementContainsText(
      "paragraph3",
      "You must wait for the outcome of Customs’ checks."
    )
  }

  "must render correct link text" in {
    assertSpecificElementContainsText("link-text", "You must wait for the outcome of Customs’ checks. Check your departure declarations for further updates.")
  }

  "must not render what happens next section" in {
    assertNotRenderedById(doc, "what-happens-next")
  }

  behave like pageWithLink(
    "view-all-declarations",
    "Check your departure declarations",
    controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad().url
  )

}