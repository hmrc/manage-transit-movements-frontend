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
import models.departureP5.BusinessRejectionType.*
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ReviewPrelodgedDeclarationErrorsP5ViewModel(
  tableRows: Seq[Seq[TableRow]],
  lrn: String,
  multipleErrors: Boolean
) {

  def title(implicit messages: Messages): String = messages("prelodged.declaration.ie056.review.message.title")

  def heading(implicit messages: Messages): String = messages("prelodged.declaration.ie056.review.message.heading")

  def paragraph1(implicit messages: Messages): String = if (multipleErrors) {
    messages("prelodged.declaration.ie056.review.message.paragraph1.plural")
  } else {
    messages("prelodged.declaration.ie056.review.message.paragraph1.singular")
  }

  def paragraph2(implicit messages: Messages): String = if (multipleErrors) {
    messages("prelodged.declaration.ie056.review.message.paragraph2.plural")
  } else {
    messages("prelodged.declaration.ie056.review.message.paragraph2.singular")
  }

  def hyperlink(implicit messages: Messages): String = messages("prelodged.declaration.ie056.review.message.hyperlink")

  def tableHeadCells(implicit messages: Messages): Seq[HeadCell] = Seq(
    HeadCell(Text(messages("error.table.errorCode"))),
    HeadCell(Text(messages("error.table.errorReason"))),
    HeadCell(Text(messages("error.table.pointer"))),
    HeadCell(Text(messages("error.table.attributeValue")))
  )

}

object ReviewPrelodgedDeclarationErrorsP5ViewModel {

  class ReviewPrelodgedDeclarationErrorsP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      functionalErrors: Seq[FunctionalErrorType04],
      lrn: String
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[ReviewPrelodgedDeclarationErrorsP5ViewModel] = {

      val helper = new RejectionMessageP5MessageHelper(Nil) // TODO - fix

      Future.successful {
        new ReviewPrelodgedDeclarationErrorsP5ViewModel(
          helper.tableRows(),
          lrn,
          functionalErrors.length > 1
        )
      }
    }
  }
}
