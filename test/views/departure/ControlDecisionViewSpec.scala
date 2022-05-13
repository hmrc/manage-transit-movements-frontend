/*
 * Copyright 2022 HM Revenue & Customs
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

package views.departure

import generators.Generators
import models.LocalReferenceNumber
import models.departure.ControlDecision
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.behaviours.SummaryListViewBehaviours
import views.html.departure.ControlDecisionView
import views.utils.ViewUtils.RichControlDecision

class ControlDecisionViewSpec extends SummaryListViewBehaviours with Generators {

  private val controlDecision = arbitrary[ControlDecision].sample.value

  override def summaryLists: Seq[SummaryList] = Seq(controlDecision.toSummaryList(lrn))

  private def applyView(controlDecisionMessage: ControlDecision, lrn: LocalReferenceNumber): HtmlFormat.Appendable =
    injector.instanceOf[ControlDecisionView].apply(controlDecisionMessage, lrn)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(controlDecision, lrn)

  override val prefix: String = "controlDecision"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithContent("p", "You must wait for the outcome of your customs check.")
}
