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

import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import models.departureP5.BusinessRejectionType.*
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.BarViewModel

import javax.inject.Inject

case class RejectionMessageP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1Prefix: String,
  paragraph1: String,
  paragraph2: String,
  hyperlink: Option[String],
  functionalErrors: FunctionalErrorsWithoutSection,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  href: Call
) extends BarViewModel[FunctionalErrorWithoutSection, FunctionalErrorsWithoutSection]

object RejectionMessageP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithoutSection,
    lrn: String,
    businessRejectionType: DepartureBusinessRejectionType,
    currentPage: Int,
    numberOfErrorsPerPage: Int,
    href: Call
  )(implicit messages: Messages): RejectionMessageP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val plural: Boolean = businessRejectionType match {
      case AmendmentRejection   => true
      case DeclarationRejection => multipleErrors
    }

    val paragraph1: String = if (plural) {
      messages("departure.ie056.message.paragraph1.plural")
    } else {
      messages("departure.ie056.message.paragraph1.singular")
    }

    val paragraph2: String = if (plural) {
      messages("departure.ie056.message.paragraph2.plural")
    } else {
      messages("departure.ie056.message.paragraph2.singular")
    }

    val hyperlink: Option[String] = businessRejectionType match {
      case AmendmentRejection   => None
      case DeclarationRejection => Some(messages("departure.ie056.message.hyperlink"))
    }

    new RejectionMessageP5ViewModel(
      title = messages("departure.ie056.message.title"),
      heading = messages("departure.ie056.message.heading"),
      caption = ???,
      paragraph1Prefix = messages("departure.ie056.message.paragraph1.prefix", lrn),
      paragraph1 = paragraph1,
      paragraph2 = paragraph2,
      hyperlink = hyperlink,
      functionalErrors = functionalErrors,
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfErrorsPerPage,
      href = href
    )
  }

  class RejectionMessageP5ViewModelProvider @Inject() {

    def apply(
      functionalErrors: FunctionalErrorsWithoutSection,
      lrn: String,
      businessRejectionType: DepartureBusinessRejectionType,
      currentPage: Int,
      numberOfErrorsPerPage: Int,
      href: Call
    )(implicit messages: Messages): RejectionMessageP5ViewModel =
      RejectionMessageP5ViewModel(
        functionalErrors,
        lrn,
        businessRejectionType,
        currentPage,
        numberOfErrorsPerPage,
        href
      )
  }
}
