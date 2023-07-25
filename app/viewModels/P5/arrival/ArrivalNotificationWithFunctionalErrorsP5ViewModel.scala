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

import models.arrivalP5.IE057MessageData
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ArrivalNotificationWithFunctionalErrorsP5ViewModel(sections: Seq[Section], mrn: String, multipleErrors: Boolean) {

  def title(implicit messages: Messages): String = messages("arrival.ie057.review.notification.message.title")

  def heading(implicit messages: Messages): String = messages("arrival.ie057.review.notification.message.heading")

  def paragraph1(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "arrival.ie057.review.notification.message.paragraph1.plural"
    )
  } else {
    messages(
      "arrival.ie057.review.notification.message.paragraph1.singular"
    )
  }

  def paragraph2Prefix(implicit messages: Messages): String = messages("arrival.ie057.review.notification.message.paragraph2.prefix")
  def paragraph2Link(implicit messages: Messages): String   = messages("arrival.ie057.review.notification.message.paragraph2.link")

  def paragraph2Suffix(implicit messages: Messages): String = if (multipleErrors) {
    messages("arrival.ie057.review.notification.message.paragraph2.plural.suffix")
  } else {
    messages("arrival.ie057.review.notification.message.paragraph2.singular.suffix")
  }

  def hyperlink(implicit messages: Messages): String = messages("arrival.ie057.review.notification.message.hyperlink")

}

object ArrivalNotificationWithFunctionalErrorsP5ViewModel {

  class ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie057MessageData: IE057MessageData,
      mrn: String
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[ArrivalNotificationWithFunctionalErrorsP5ViewModel] = {
      val helper = new RejectionMessageP5MessageHelper(ie057MessageData.functionalErrors, referenceDataService)

      val multipleErrors = ie057MessageData.functionalErrors.length > 1
      val sections       = Seq(helper.errorSection())
      Future
        .sequence(sections)
        .map(
          sec => ArrivalNotificationWithFunctionalErrorsP5ViewModel(sec, mrn, multipleErrors)
        )

    }
  }
}
