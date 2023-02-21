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

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import org.jsoup.nodes.Element
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import views.behaviours.{ViewBehaviours, YesNoViewBehaviours}
import views.html.departure.drafts.DeleteDraftDepartureYesNoView

class DeleteDraftDepartureYesNoViewSpec extends YesNoViewBehaviours with ScalaCheckPropertyChecks {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[DeleteDraftDepartureYesNoView].apply(form, lrn.toString(), 1, 1, None)(fakeRequest, messages)

  override val prefix: String = "departure.drafts.deleteDraftDepartureYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", "Once you delete it, you will not be able to retrieve it.")

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Confirm")

  "must display table with local reference number label and correct LRN" in {
    def elementWithVisibleText(element: Element, text: String): Unit =
      element.ownText() mustBe text

    val lrnLabel = doc.getElementsByClass("govuk-summary-list__key").head
    val lrnValue = doc.getElementsByClass("govuk-summary-list__value").head

    behave like elementWithVisibleText(lrnLabel, "Local reference number")
    behave like elementWithVisibleText(lrnValue, lrn.toString)
  }
}
