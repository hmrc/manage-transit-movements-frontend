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
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject

case class GoodsUnderControlP5ViewModel(sections: Seq[Section])

object GoodsUnderControlP5ViewModel {

  def apply(ie060MessageData: IE060MessageData)(implicit messages: Messages): GoodsUnderControlP5ViewModel =
    new GoodsUnderControlP5ViewModelProvider()(ie060MessageData)

  class GoodsUnderControlP5ViewModelProvider @Inject() () {

    def apply(ie060MessageData: IE060MessageData)(implicit messages: Messages): GoodsUnderControlP5ViewModel = {
      val helper = new GoodsUnderControlP5MessageHelper(ie060MessageData)

      val sections = Seq(helper.buildGoodsUnderControlSection()) ++ helper.controlInformationSection() ++ helper.documentSection()
      new GoodsUnderControlP5ViewModel(sections)
    }

    def fetchWhatHappensNext(ie060MessageData: IE060MessageData, customsOffice: Option[CustomsOffice])(implicit messages: Messages): String =
      customsOffice match {
        case Some(CustomsOffice(_, name, Some(phone))) =>
          (name.nonEmpty, phone.nonEmpty) match {
            case (true, true) => messages("goodsUnderControl.telephoneAvailable", name, phone)
            case _            => messages("goodsUnderControl.telephoneNotAvailable", name)
          }
        case Some(CustomsOffice(_, name, None)) if name.nonEmpty        => messages("goodsUnderControl.telephoneNotAvailable", name)
        case Some(CustomsOffice(id, "", Some(phone))) if phone.nonEmpty => messages("goodsUnderControl.teleAvailAndOfficeNameNotAvail", id, phone)
        case _ =>
          messages("goodsUnderControl.teleNotAvailAndOfficeNameNotAvail", ie060MessageData.CustomsOfficeOfDeparture.referenceNumber)
      }
  }
}
