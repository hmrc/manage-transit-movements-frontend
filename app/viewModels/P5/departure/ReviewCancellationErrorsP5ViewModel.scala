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

import models.departureP5.FunctionalError
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ReviewCancellationErrorsP5ViewModel(tableRows: Seq[Seq[TableRow]], lrn: String, multipleErrors: Boolean) {

  def title(implicit messages: Messages): String = messages("departure.ie056.review.cancellation.message.title")

  def heading(implicit messages: Messages): String = messages("departure.ie056.review.cancellation.message.heading")

  def paragraph1(implicit messages: Messages): String = if (multipleErrors) {
    messages(
      "departure.ie056.review.cancellation.message.paragraph1.plural"
    )
  } else {
    messages(
      "departure.ie056.review.cancellation.message.paragraph1.singular"
    )
  }

  def paragraph2Prefix(implicit messages: Messages): String = messages("departure.ie056.review.cancellation.message.paragraph2.prefix")
  def paragraph2Link(implicit messages: Messages): String   = messages("departure.ie056.review.cancellation.message.paragraph2.link")

  def paragraph2Suffix(implicit messages: Messages): String = if (multipleErrors) {
    messages("departure.ie056.review.cancellation.message.paragraph2.plural.suffix")
  } else {
    messages("departure.ie056.review.cancellation.message.paragraph2.singular.suffix")
  }

  def hyperlink(implicit messages: Messages): String = messages("departure.ie056.review.cancellation.message.hyperlink")

  val viewDeparturesLink: String = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url

  def tableHeadCells(implicit messages: Messages): Seq[HeadCell] = Seq(
    HeadCell(Text(messages("error.table.errorCode"))),
    HeadCell(Text(messages("error.table.errorReason")))
  )

}

object ReviewCancellationErrorsP5ViewModel {

  class ReviewCancellationErrorsP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      functionalErrors: Seq[FunctionalError],
      lrn: String
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[ReviewCancellationErrorsP5ViewModel] = {

      val helper: RejectionMessageP5MessageHelper = new RejectionMessageP5MessageHelper(functionalErrors, referenceDataService)

      val multipleErrors = functionalErrors.length > 1
      helper.tableRows().map(ReviewCancellationErrorsP5ViewModel(_, lrn, multipleErrors))

    }
  }
}
