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
import models.referenceData.{ControlType, CustomsOffice}
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class GoodsUnderControlP5ViewModel(sections: Seq[Section])

object GoodsUnderControlP5ViewModel {

  class GoodsUnderControlP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie060MessageData: IE060MessageData
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[GoodsUnderControlP5ViewModel] = {
      val helper = new GoodsUnderControlP5MessageHelper(ie060MessageData, referenceDataService)
      helper.controlInformationSection().map {
        controlInfoSections =>
          val sections = Seq(helper.buildGoodsUnderControlSection()) ++ controlInfoSections ++ helper.documentSection()
          new GoodsUnderControlP5ViewModel(sections)
      }
    }
  }
}
