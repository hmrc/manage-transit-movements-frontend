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

import models.departureP5.IE060MessageData
import play.api.i18n.Messages
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject

case class GoodsUnderControlP5ViewModel(sections: Seq[Section], notificationType: String) {

  def notificationTypeTitle(implicit messages: Messages): String = notificationType match {
    case "1" => messages("departure.ie060.message.notificationType1.title")
    case _   => messages("departure.ie060.message.title")
  }

  def notificationTypeHeading(implicit messages: Messages): String = notificationType match {
    case "1" => messages("departure.ie060.message.notificationType1.heading")
    case _   => messages("departure.ie060.message.heading")
  }

}

object GoodsUnderControlP5ViewModel {

  class GoodsUnderControlP5ViewModelProvider @Inject() () {

    def apply(ie060MessageData: IE060MessageData)(implicit messages: Messages): GoodsUnderControlP5ViewModel = {
      val helper = new GoodsUnderControlP5MessageHelper(ie060MessageData)

      val notificationType = ie060MessageData.TransitOperation.notificationType

      val sections = notificationType match {
        case "1" => Seq(helper.buildGoodsUnderControlSection()) ++ helper.documentSection()
        case _   => Seq(helper.buildGoodsUnderControlSection()) ++ helper.controlInformationSection() ++ helper.documentSection()
      }

      new GoodsUnderControlP5ViewModel(sections, notificationType)
    }
  }
}
