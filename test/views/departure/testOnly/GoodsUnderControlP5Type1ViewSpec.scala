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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.departure.{CustomsOfficeContactViewModel, GoodsUnderControlP5ViewModel}
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departure.P5.TestOnlyGoodsUnderControlP5View

class GoodsUnderControlP5Type1ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  private val customsOffice: CustomsOffice   = arbitrary[CustomsOffice].sample.value
  private val customsReferenceNumber: String = Gen.alphaNumStr.sample.value
  private val notificationType               = "1"

  override val prefix: String = "departure.ie060.message.notificationType1"

  private val goodsUnderControlP5ViewModel: GoodsUnderControlP5ViewModel   = new GoodsUnderControlP5ViewModel(sections, notificationType)
  private val customsOfficeContactViewModel: CustomsOfficeContactViewModel = CustomsOfficeContactViewModel(customsReferenceNumber, Some(customsOffice))

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[TestOnlyGoodsUnderControlP5View]
      .apply(goodsUnderControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(fakeRequest, messages)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

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
}
