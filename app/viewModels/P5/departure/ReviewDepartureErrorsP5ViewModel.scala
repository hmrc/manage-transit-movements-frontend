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

import models.FunctionalErrors.FunctionalErrorsWithSection
import models.departureP5.BusinessRejectionType.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table

case class ReviewDepartureErrorsP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1: String,
  paragraph2: String,
  hyperlink: Option[String],
  table: Table
)

object ReviewDepartureErrorsP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithSection,
    lrn: String,
    businessRejectionType: DepartureBusinessRejectionType,
    currentPage: Int,
    numberOfErrorsPerPage: Int
  )(implicit messages: Messages): ReviewDepartureErrorsP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val heading: String = messages("departure.ie056.review.message.heading")

    val paragraph1: String = businessRejectionType match {
      case AmendmentRejection =>
        if (multipleErrors) {
          messages("departure.ie056.review.message.paragraph1.amendment.plural")
        } else {
          messages("departure.ie056.review.message.paragraph1.amendment.singular")
        }
      case DeclarationRejection =>
        if (multipleErrors) {
          messages("departure.ie056.review.message.paragraph1.plural")
        } else {
          messages("departure.ie056.review.message.paragraph1.singular")
        }
    }

    val paragraph2: String = if (multipleErrors) {
      messages("departure.ie056.review.message.paragraph2.plural")
    } else {
      messages("departure.ie056.review.message.paragraph2.singular")
    }

    val hyperlink: Option[String] = businessRejectionType match {
      case AmendmentRejection   => None
      case DeclarationRejection => Some(messages("departure.ie056.review.message.hyperlink"))
    }

    new ReviewDepartureErrorsP5ViewModel(
      title = messages("departure.ie056.review.message.title"),
      heading = heading,
      caption = messages("departure.messages.caption", lrn),
      paragraph1 = paragraph1,
      paragraph2 = paragraph2,
      hyperlink = hyperlink,
      table = functionalErrors.paginate(currentPage, numberOfErrorsPerPage).toTable
    )
  }

  class ReviewDepartureErrorsP5ViewModelProvider {

    def apply(
      functionalErrors: FunctionalErrorsWithSection,
      lrn: String,
      businessRejectionType: DepartureBusinessRejectionType,
      currentPage: Int,
      numberOfErrorsPerPage: Int
    )(implicit messages: Messages): ReviewDepartureErrorsP5ViewModel =
      ReviewDepartureErrorsP5ViewModel.apply(
        functionalErrors,
        lrn,
        businessRejectionType,
        currentPage,
        numberOfErrorsPerPage
      )
  }

}
