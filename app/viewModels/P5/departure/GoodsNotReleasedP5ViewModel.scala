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

package viewModels.P5.departure

import generated.CC051CType
import play.api.i18n.Messages
import utils.GoodsNotReleasedP5Helper
import viewModels.sections.Section

case class GoodsNotReleasedP5ViewModel(
  sections: Seq[Section],
  lrn: String
) {

  def title(implicit messages: Messages): String     = messages("departure.notReleased.title")
  def heading(implicit messages: Messages): String   = messages("departure.notReleased.heading")
  def paragraph(implicit messages: Messages): String = messages("departure.notReleased.paragraph", lrn)
  def hyperlink(implicit messages: Messages): String = messages("departure.notReleased.anotherDeparture.hyperlink")
  def caption(implicit messages: Messages): String   = messages("departure.messages.caption", lrn)
  def link(implicit messages: Messages): String      = messages("departure.notReleased.link")
  def heading2(implicit messages: Messages): String  = messages("departure.notReleased.whatHappensNext")

}

object GoodsNotReleasedP5ViewModel {

  class GoodsNotReleasedP5ViewModelProvider() {

    def apply(
      ie051: CC051CType,
      lrn: String
    )(implicit messages: Messages): GoodsNotReleasedP5ViewModel = {
      val helper = new GoodsNotReleasedP5Helper(ie051)

      new GoodsNotReleasedP5ViewModel(Seq(helper.buildDetailsSection), lrn)
    }

  }

}
