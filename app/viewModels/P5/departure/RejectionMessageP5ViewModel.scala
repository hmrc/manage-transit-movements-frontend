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
import play.api.mvc.Call
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class RejectionMessageP5ViewModel(sections: Seq[Section], lrn: Option[String], multipleErrors: Boolean) {
  def title(implicit messages: Messages): String = messages("departure.ie056.message.title")

  def heading(implicit messages: Messages): String = messages("departure.ie056.message.heading")

  def paragraph1Prefix(implicit messages: Messages): String = messages("departure.ie056.message.paragraph1.prefix", lrn.getOrElse(""))

  def paragraph1Suffix(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "departure.ie056.message.paragraph1.plural.suffix"
    )
  } else {
    messages(
      "departure.ie056.message.paragraph1.singular.suffix"
    )
  }

  def paragraph2Prefix(implicit messages: Messages): String = messages("departure.ie056.message.paragraph2.prefix")
  def paragraph2Link(implicit messages: Messages): String   = messages("departure.ie056.message.paragraph2.link")

  def paragraph2Suffix(implicit messages: Messages): String = if (multipleErrors) {
    messages("departure.ie056.message.paragraph2.plural.suffix")
  } else {
    messages("departure.ie056.message.paragraph2.singular.suffix")
  }

  def hyperlink(implicit messages: Messages): String = messages("departure.ie056.message.hyperlink")

}

object RejectionMessageP5ViewModel {

  class RejectionMessageP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie056MessageData: IE056MessageData
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[RejectionMessageP5ViewModel] = {
      val helper = new RejectionMessageP5MessageHelper(ie056MessageData, referenceDataService)

      val lrn            = ie056MessageData.TransitOperation.LRN
      val multipleErrors = ie056MessageData.functionalErrorToSeq.length > 1
      val sections       = Seq(helper.errorSection())
      Future
        .sequence(sections)
        .map(
          sec => RejectionMessageP5ViewModel(sec, lrn, multipleErrors)
        )

    }
  }
}
