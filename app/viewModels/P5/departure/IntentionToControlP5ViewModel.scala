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

import generated.CC060CType
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import utils.IntentionToControlP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject

case class IntentionToControlP5ViewModel(sections: Seq[Section], lrn: Option[String], customsOffice: CustomsOffice) extends CustomsOfficeContactViewModel {

  def title(implicit messages: Messages): String = messages("departure.ie060.message.prelodged.title")

  def heading(implicit messages: Messages): String = messages("departure.ie060.message.prelodged.heading")

  def paragraph1(implicit messages: Messages): String = messages("departure.ie060.message.prelodged.paragraph1")

  def paragraph2(implicit messages: Messages): String = messages("departure.ie060.message.prelodged.paragraph2")

  def paragraph3(implicit messages: Messages): String = messages("departure.ie060.message.prelodged.paragraph3")
}

object IntentionToControlP5ViewModel {

  class IntentionToControlP5ViewModelProvider @Inject() {

    def apply(
      ie060: CC060CType,
      customsOffice: CustomsOffice
    )(implicit messages: Messages): IntentionToControlP5ViewModel = {
      val helper = new IntentionToControlP5MessageHelper(ie060, customsOffice)

      val lrn = ie060.TransitOperation.LRN

      val intentionToControlSection = helper.buildIntentionToControlSection()

      val sections = Seq(intentionToControlSection)

      new IntentionToControlP5ViewModel(sections, lrn, customsOffice: CustomsOffice)
    }
  }
}
