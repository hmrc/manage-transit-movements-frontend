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

import models.FunctionalError
import models.departureP5.BusinessRejectionType.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import utils.RejectionMessageP5MessageHelper

case class ReviewDepartureErrorsP5ViewModel(
  table: Table,
  lrn: String,
  multipleErrors: Boolean,
  businessRejectionType: DepartureBusinessRejectionType
) {

  def title(implicit messages: Messages): String = messages("departure.ie056.review.message.title")

  def heading(implicit messages: Messages): String = messages("departure.ie056.review.message.heading")

  def paragraph1Prefix(implicit messages: Messages): String = messages("departure.ie056.review.message.paragraph1.prefix", lrn)

  def paragraph1(implicit messages: Messages): String = businessRejectionType match {
    case AmendmentRejection   => paragraph1Amendment
    case DeclarationRejection => paragraph1NoAmendment
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

  def paragraph2(implicit messages: Messages): String = if (multipleErrors) {
    messages("departure.ie056.review.message.paragraph2.plural")
  } else {
    messages("departure.ie056.review.message.paragraph2.singular")
  }

  def hyperlink(implicit messages: Messages): Option[String] = businessRejectionType match {
    case AmendmentRejection   => None
    case DeclarationRejection => Some(messages("departure.ie056.review.message.hyperlink"))
  }

}

object ReviewDepartureErrorsP5ViewModel {

  def apply(
    functionalErrors: Seq[FunctionalError],
    lrn: String,
    businessRejectionType: DepartureBusinessRejectionType
  )(implicit messages: Messages): ReviewDepartureErrorsP5ViewModel =
    new ReviewDepartureErrorsP5ViewModel(
      table = functionalErrors.toTableOfDepartureErrors,
      lrn = lrn,
      multipleErrors = functionalErrors.length > 1,
      businessRejectionType = businessRejectionType
    )

  class ReviewDepartureErrorsP5ViewModelProvider {

    def apply(
      functionalErrors: Seq[FunctionalError],
      lrn: String,
      businessRejectionType: DepartureBusinessRejectionType
    )(implicit messages: Messages): ReviewDepartureErrorsP5ViewModel =
      ReviewDepartureErrorsP5ViewModel.apply(functionalErrors, lrn, businessRejectionType)
  }

}
