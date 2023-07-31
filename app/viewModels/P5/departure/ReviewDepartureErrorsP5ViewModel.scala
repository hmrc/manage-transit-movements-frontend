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

import models.departureP5.IE056MessageData
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ReviewDepartureErrorsP5ViewModel(sections: Seq[Section], lrn: String, multipleErrors: Boolean) {

  def title(implicit messages: Messages): String = messages("departure.ie056.review.message.title")

  def heading(implicit messages: Messages): String = messages("departure.ie056.review.message.heading")

  def paragraph1Prefix(implicit messages: Messages): String = messages("departure.ie056.review.message.paragraph1.prefix", lrn)

  def paragraph1(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "departure.ie056.review.message.paragraph1.plural"
    )
  } else {
    messages(
      "departure.ie056.review.message.paragraph1.singular"
    )
  }

  def paragraph2Prefix(implicit messages: Messages): String = messages("departure.ie056.review.message.paragraph2.prefix")
  def paragraph2Link(implicit messages: Messages): String   = messages("departure.ie056.review.message.paragraph2.link")

  def paragraph2Suffix(implicit messages: Messages): String = if (multipleErrors) {
    messages("departure.ie056.review.message.paragraph2.plural.suffix")
  } else {
    messages("departure.ie056.review.message.paragraph2.singular.suffix")
  }

  def hyperlink(implicit messages: Messages): String = messages("departure.ie056.review.message.hyperlink")

}

object ReviewDepartureErrorsP5ViewModel {

  class ReviewDepartureErrorsP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie056MessageData: IE056MessageData,
      lrn: String,
      from: Int,
      to: Int
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[ReviewDepartureErrorsP5ViewModel] = {
      val paged = ie056MessageData.functionalErrors.sortBy(x => x.errorCode).slice(from, to)
      val helper = new RejectionMessageP5MessageHelper(paged, referenceDataService)

      val multipleErrors = ie056MessageData.functionalErrors.length > 1
      val sections       = Seq(helper.errorSection())
      Future
        .sequence(sections)
        .map(
          sec => ReviewDepartureErrorsP5ViewModel(sec, lrn, multipleErrors)
        )

    }
  }
}
