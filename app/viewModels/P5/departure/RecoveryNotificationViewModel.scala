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

import models.departureP5.IE035MessageData
import play.api.i18n.Messages
import utils.RecoveryNotificationHelper
import viewModels.sections.Section

import javax.inject.Inject

case class RecoveryNotificationViewModel(sections: Seq[Section]) {

  def title(implicit messages: Messages): String           = messages("departure.ie035.message.title")
  def heading(implicit messages: Messages): String         = messages("departure.ie035.message.heading")
  def paragraph1(implicit messages: Messages): String      = messages("departure.ie035.message.paragraph1")
  def paragraph2(implicit messages: Messages): String      = messages("departure.ie035.message.paragraph2")
  def whatHappensNext(implicit messages: Messages): String = messages("departure.ie035.message.h2")
  def paragraph3(implicit messages: Messages): String      = messages("departure.ie035.message.paragraph3")
}

object RecoveryNotificationViewModel {

  def apply(
    IE035MessageData: IE035MessageData
  )(implicit
    messages: Messages
  ): RecoveryNotificationViewModel =
    new RecoveryNotificationViewModelProvider().apply(IE035MessageData)

  class RecoveryNotificationViewModelProvider @Inject() () {

    def apply(
      IE035MessageData: IE035MessageData
    )(implicit messages: Messages): RecoveryNotificationViewModel = {
      val helper = new RecoveryNotificationHelper(IE035MessageData)

      new RecoveryNotificationViewModel(Seq(helper.buildRecoveryNotificationSection))

    }
  }
}
