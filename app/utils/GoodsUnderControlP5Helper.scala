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

package utils

import models.departureP5.{IE060Data, IE060MessageData}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate

class GoodsUnderControlP5Helper(ie060MessageData: IE060MessageData)(implicit messages: Messages)  {

  def unloadingType: Option[SummaryListRow] = getAnswerAndBuildRow[UnloadingType](
    page = UnloadingTypePage,
    formatAnswer = formatEnumAsText(UnloadingType.messageKeyPrefix),
    prefix = "unloadingType.checkYourAnswers",
    args = messages("unloadingType.hidden"),
    id = Some("change-unloaded-type"),
    call = Some(controllers.routes.UnloadingTypeController.onPageLoad(arrivalId, CheckMode))
  )

  def goodsUnloadedDate: Option[SummaryListRow] = getAnswerAndBuildRow[LocalDate](
    page = DateGoodsUnloadedPage,
    formatAnswer = formatAsDate,
    prefix = "checkYourAnswers.rowHeadings.goodsUnloadedDate",
    id = Some("change-goods-unloaded-date"),
    call = Some(controllers.routes.DateGoodsUnloadedController.onPageLoad(arrivalId, CheckMode))
  )



}
