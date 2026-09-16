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

package viewModels.departure

import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import viewModels.ViewModelWithCustomsOffice

case class CancellationNotificationErrorsViewModel(
  lrn: String,
  customsOffice: CustomsOffice
) extends ViewModelWithCustomsOffice {

  override val prefix: String = "cancellation.notification.customsOfficeContact"

  def title(implicit messages: Messages): String = messages("cancellation.notification.errors.message.title")

  def heading(implicit messages: Messages): String = messages("cancellation.notification.errors.message.heading")

  def paragraph1(implicit messages: Messages): String =
    messages("cancellation.notification.errors.message")

  def hyperlink(implicit messages: Messages): String = messages("cancellation.notification.errors.message.viewDepartureDeclarations")

}

object CancellationNotificationErrorsViewModel {

  class CancellationNotificationErrorsViewModelProvider {

    def apply(
      lrn: String,
      customsOffice: CustomsOffice
    ): CancellationNotificationErrorsViewModel =
      CancellationNotificationErrorsViewModel(lrn, customsOffice)
  }
}
