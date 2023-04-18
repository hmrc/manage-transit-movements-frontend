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

import models.departureP5.{IE060Data, IE060MessageData}
import play.api.i18n.Messages
import utils.GoodsUnderControlP5Helper

import javax.inject.Inject

case class GoodsUnderControlP5ViewModel(ie060MessageData: IE060MessageData)

object GoodsUnderControlP5ViewModel {

  def apply(ie060MessageData: IE060MessageData)(implicit messages: Messages): GoodsUnderControlP5ViewModel =
    new GoodsUnderControlP5ViewModelProvider()(ie060MessageData)

  class GoodsUnderControlP5ViewModelProvider @Inject() () {

    def apply(ie060MessageData: IE060MessageData)(implicit messages: Messages): GoodsUnderControlP5ViewModel = {
      val helper = new GoodsUnderControlP5Helper(ie060MessageData)

      new GoodsUnderControlP5ViewModel(ie060MessageData)
    }
  }
}
