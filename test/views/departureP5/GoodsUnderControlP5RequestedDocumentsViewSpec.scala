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
import viewModels.P5.departure.GoodsUnderControlP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.GoodsUnderControlP5View

class GoodsUnderControlP5RequestedDocumentsViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  private val customsOffice: CustomsOffice = arbitrary[CustomsOffice].sample.value

  override val prefix: String = "departure.ie060.message.requestedDocuments"

  private val goodsUnderControlP5ViewModel: GoodsUnderControlP5ViewModel = new GoodsUnderControlP5ViewModel(sections, true, Some(lrn.toString), customsOffice)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[GoodsUnderControlP5View]
      .apply(goodsUnderControlP5ViewModel, departureIdP5)(fakeRequest, messages)

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
    "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
  )

  behave like pageWithSpecificContent(
    "paragraph2",
    "While awaiting the documentation, the goods will remain under supervision at the customs office of departure."
  )

  behave like pageWithSpecificContent(
    "paragraph3",
    "You must contact the customs office of departure directly to share the requested documentation."
  )

  "must render what happens next" in {
    assertRenderedById(doc, "what-happens-next")
  }

  "must not render link" in {
    assertNotRenderedById(doc, "link-text")
  }

}
