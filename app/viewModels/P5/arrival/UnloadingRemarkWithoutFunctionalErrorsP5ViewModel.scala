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

import models.referenceData.CustomsOffice
import play.api.i18n.Messages

case class UnloadingRemarkWithoutFunctionalErrorsP5ViewModel(mrn: String, customsOfficeReferenceId: String, customsOffice: Option[CustomsOffice]) {

  def title(implicit messages: Messages): String = messages("arrival.notification.unloading.errors.message.title")

  def heading(implicit messages: Messages): String = messages("arrival.notification.unloading.errors.message.heading")

  def paragraph1(implicit messages: Messages): String = messages("arrival.notification.unloading.errors.message.noerrors")

  def customsOfficeContent(implicit messages: Messages): String =
    customsOffice match {
      case Some(CustomsOffice(id, _, _)) => customsOfficeNameAndNumber(messages, id)
      case _ =>
        messages("arrival.notification.unloading.customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", customsOfficeReferenceId)
    }

  private def customsOfficeNameAndNumber(messages: Messages, id: String): String =
    (customsOffice.get.nameOption, customsOffice.get.phoneOption) match {
      case (Some(name), Some(phone)) => messages("arrival.notification.unloading.customsOfficeContact.telephoneAvailable", name, phone)
      case (None, Some(phone))       => messages("arrival.notification.unloading.customsOfficeContact.teleAvailAndOfficeNameNotAvail", id, phone)
      case (Some(name), None)        => messages("arrival.notification.unloading.customsOfficeContact.telephoneNotAvailable", name)
      case _                         => messages("arrival.notification.unloading.customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", id)
    }
  def hyperlink(implicit messages: Messages): String = messages("arrival.notification.unloading.errors.message.hyperlink")

}

object UnloadingRemarkWithoutFunctionalErrorsP5ViewModel {

  class UnloadingRemarkWithoutFunctionalErrorsP5ViewModelProvider {

    def apply(mrn: String, customsOfficeReferenceId: String, customsOffice: Option[CustomsOffice]): UnloadingRemarkWithoutFunctionalErrorsP5ViewModel =
      UnloadingRemarkWithoutFunctionalErrorsP5ViewModel(mrn, customsOfficeReferenceId, customsOffice)
  }

}
