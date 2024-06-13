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

import generated.FunctionalErrorType04
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ReviewDepartureErrorsP5ViewModel(tableRows: Seq[Seq[TableRow]], lrn: String, multipleErrors: Boolean, isAmendmentJourney: Boolean) {

  def title(implicit messages: Messages): String = messages("departure.ie056.review.message.title")

  def heading(implicit messages: Messages): String = messages("departure.ie056.review.message.heading")

  def paragraph1Prefix(implicit messages: Messages): String = messages("departure.ie056.review.message.paragraph1.prefix", lrn)

  def paragraph1(implicit messages: Messages): String = if (isAmendmentJourney) {
    paragraph1Amendment
  } else {
    paragraph1NoAmendment
  }

  def paragraph1NoAmendment(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "departure.ie056.review.message.paragraph1.plural"
    )
  } else {
    messages(
      "departure.ie056.review.message.paragraph1.singular"
    )
  }

  def paragraph1Amendment(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "departure.ie056.review.message.paragraph1.amendment.plural"
    )
  } else {
    messages(
      "departure.ie056.review.message.paragraph1.amendment.singular"
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

  def tableHeadCells(implicit messages: Messages): Seq[HeadCell] = Seq(
    HeadCell(Text(messages("error.table.errorCode"))),
    HeadCell(Text(messages("error.table.errorReason"))),
    HeadCell(Text(messages("error.table.pointer"))),
    HeadCell(Text(messages("error.table.attributeValue")))
  )

}

object ReviewDepartureErrorsP5ViewModel {

  class ReviewDepartureErrorsP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      functionalErrors: Seq[FunctionalErrorType04],
      lrn: String,
      isAmendmentJourney: Boolean
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[ReviewDepartureErrorsP5ViewModel] = {

      val helper         = new RejectionMessageP5MessageHelper(functionalErrors, referenceDataService)
      val multipleErrors = functionalErrors.length > 1
      helper.tableRows().map(ReviewDepartureErrorsP5ViewModel(_, lrn, multipleErrors, isAmendmentJourney))
    }
  }
}
