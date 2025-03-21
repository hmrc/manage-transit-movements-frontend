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

package viewModels.P5.arrival

import play.api.i18n.Messages

case class ArrivalNotificationWithoutFunctionalErrorP5ViewModel(mrn: String) {
  def title(implicit messages: Messages): String = messages("arrival.notification.errors.message.title")

  def heading(implicit messages: Messages): String = messages("arrival.notification.errors.message.heading")

  def paragraph1(implicit messages: Messages): String = messages(s"arrival.notification.errors.message.noerrors")

  def paragraph2(implicit messages: Messages): String = messages(s"arrival.notification.errors.message.paragraph2")

  def link1(implicit messages: Messages): String = messages("arrival.notification.errors.message.link1")

  def link2(implicit messages: Messages): String = messages("arrival.notification.errors.message.link2")

}

object ArrivalNotificationWithoutFunctionalErrorP5ViewModel {

  class ArrivalNotificationWithoutFunctionalErrorP5ViewModelProvider {
    def apply(mrn: String): ArrivalNotificationWithoutFunctionalErrorP5ViewModel = ArrivalNotificationWithoutFunctionalErrorP5ViewModel(mrn)
  }

}
