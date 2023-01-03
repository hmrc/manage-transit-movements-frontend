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

package views.departure

import generators.Generators
import models.departure.NoReleaseForTransitMessage
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.behaviours.SummaryListViewBehaviours
import views.html.departure.NoReleaseForTransitView
import views.utils.ViewUtils._

class NoReleaseForTransitViewSpec extends SummaryListViewBehaviours with Generators {

  private val message: NoReleaseForTransitMessage = arbitrary[NoReleaseForTransitMessage].sample.value

  override def summaryLists: Seq[SummaryList] = message.toSummaryLists

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[NoReleaseForTransitView].apply(message)(fakeRequest, messages)

  override val prefix: String = "noReleaseForTransit"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithContent("p", "You must contact the office of departure for more details.")
}
