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

package viewModels.P5

import models.referenceData.CustomsOffice
import play.api.i18n.Messages

trait ViewModelWithCustomsOffice {

  val prefix: String

  val customsOffice: Either[String, CustomsOffice]

  def customsOfficeContent(implicit messages: Messages): String =
    customsOffice match {
      case Right(CustomsOffice(_, name, Some(phoneNumber))) if name.nonEmpty && phoneNumber.nonEmpty =>
        messages(s"$prefix.telephoneAvailable", name, phoneNumber)
      case Right(CustomsOffice(id, _, Some(phoneNumber))) if phoneNumber.nonEmpty =>
        messages(s"$prefix.teleAvailAndOfficeNameNotAvail", id, phoneNumber)
      case Right(CustomsOffice(_, name, _)) if name.nonEmpty =>
        messages(s"$prefix.telephoneNotAvailable", name)
      case Right(CustomsOffice(id, _, _)) =>
        messages(s"$prefix.teleNotAvailAndOfficeNameNotAvail", id)
      case Left(id) =>
        messages(s"$prefix.teleNotAvailAndOfficeNameNotAvail", id)
    }

}
