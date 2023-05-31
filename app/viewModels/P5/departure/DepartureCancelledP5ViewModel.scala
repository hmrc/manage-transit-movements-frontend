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

import models.departureP5.IE009MessageData
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.DepartureCancelledP5Helper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class DepartureCancelledP5ViewModel(
  sections: Seq[Section],
  lrn: String,
  customsOfficeReferenceId: String,
  customsOffice: Option[CustomsOffice],
  isCancelled: Boolean
) {

  def title(implicit messages: Messages): String = if (isCancelled) {
    messages("departure.cancellation.message.title")
  } else {
    messages("departure.cancellation.notCancelled.message.title")
  }

  def heading(implicit messages: Messages): String = if (isCancelled) {
    messages("departure.cancellation.message.heading")
  } else {
    messages("departure.cancellation.notCancelled.message.heading")
  }

  def paragraph(implicit messages: Messages): String = if (isCancelled) {
    messages("departure.cancellation.message.paragraph", lrn)
  } else {
    messages("departure.cancellation.notCancelled.message.paragraph", lrn)
  }

  def hyperlink(implicit messages: Messages): String = messages("departure.cancellation.message.hyperlink")

  def caption(implicit messages: Messages): String = messages("departure.messages.caption", lrn)

  def customsOfficeContent(implicit messages: Messages): String =
    customsOffice match {
      case Some(CustomsOffice(id, _, _)) => customsOfficeNameAndNumber(messages, id)
      case _ =>
        messages("departure.cancellation.message.customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", customsOfficeReferenceId)
    }

  private def customsOfficeNameAndNumber(messages: Messages, id: String): String =
    (customsOffice.get.nameOption, customsOffice.get.phoneOption) match {
      case (Some(name), Some(phone)) => messages("departure.cancellation.message.customsOfficeContact.telephoneAvailable", name, phone)
      case (None, Some(phone))       => messages("departure.cancellation.message.customsOfficeContact.teleAvailAndOfficeNameNotAvail", id, phone)
      case (Some(name), None)        => messages("departure.cancellation.message.customsOfficeContact.telephoneNotAvailable", name)
      case _                         => messages("departure.cancellation.message.customsOfficeContact.teleNotAvailAndOfficeNameNotAvail", id)
    }
}

object DepartureCancelledP5ViewModel {

  class DepartureCancelledP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie009MessageData: IE009MessageData,
      lrn: String,
      customsOfficeReferenceId: String,
      customsOffice: Option[CustomsOffice],
      isCancelled: Boolean
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureCancelledP5ViewModel] = {
      val helper = new DepartureCancelledP5Helper(ie009MessageData, referenceDataService)

      helper.buildInvalidationSection.map {
        section =>
          new DepartureCancelledP5ViewModel(Seq(section), lrn, customsOfficeReferenceId, customsOffice, isCancelled)
      }
    }
  }
}