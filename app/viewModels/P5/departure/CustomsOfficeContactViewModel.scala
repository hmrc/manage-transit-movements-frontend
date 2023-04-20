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
//    val cc = customsOffice.map(c => c.copy(phoneNumber = c.phoneNumber.getOrElse("")))
    customsOffice match {
      case Some(CustomsOffice(id, name, Some(phone))) =>
        (name.nonEmpty, phone.nonEmpty) match {
          case (true, true)  => messages("customsOfficeContact.telephoneAvailable", name, phone)
          case (false, true) => messages("customsOfficeContact.teleAvailAndOfficeNameNotAvail", id, phone)
          case (true, false) => messages("customsOfficeContact.telephoneNotAvailable", name)
          case _             => messages("customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", id)
        }
      case Some(CustomsOffice(id, name, None)) =>
        name.nonEmpty match {
          case true => messages("customsOfficeContact.telephoneNotAvailable", name)
          case _    => messages("customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", id)
        }
      case _ =>
        messages("customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", customsOfficeReferenceID)
    }
}
