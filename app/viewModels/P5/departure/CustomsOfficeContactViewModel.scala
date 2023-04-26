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

import models.referenceData.CustomsOffice
import play.api.i18n.Messages

case class CustomsOfficeContactViewModel(customsOfficeReferenceID: String, customsOffice: Option[CustomsOffice]) {

  def fetchWhatHappensNext(implicit messages: Messages): String =
    customsOffice match {
      case Some(CustomsOffice(id, _, _)) =>
        matchNameAndNumber(messages, id)
      case _ =>
        messages("customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", customsOfficeReferenceID)
    }

  private def matchNameAndNumber(messages: Messages, id: String): String =
    (customsOffice.get.nameOption, customsOffice.get.phoneOption) match {
      case (Some(name), Some(phone)) => messages("customsOfficeContact.telephoneAvailable", name, phone)
      case (None, Some(phone))       => messages("customsOfficeContact.teleAvailAndOfficeNameNotAvail", id, phone)
      case (Some(name), None)        => messages("customsOfficeContact.telephoneNotAvailable", name)
      case _                         => messages("customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", id)
    }
}
