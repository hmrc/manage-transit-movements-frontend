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

import controllers.departureP5.routes
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.ErrorPaginationViewModel

import javax.inject.Inject

case class ReviewCancellationErrorsP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1: String,
  paragraph2: String,
  hyperlink: String,
  functionalErrors: FunctionalErrorsWithoutSection,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  href: Call
) extends ErrorPaginationViewModel[FunctionalErrorWithoutSection, FunctionalErrorsWithoutSection] {

  val viewDeparturesLink: String = routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
}

object ReviewCancellationErrorsP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithoutSection,
    lrn: String,
    currentPage: Int,
    numberOfErrorsPerPage: Int,
    href: Call
  )(implicit messages: Messages): ReviewCancellationErrorsP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val paragraph1: String = if (multipleErrors) {
      messages("departure.ie056.review.cancellation.message.paragraph1.plural")
    } else {
      messages("departure.ie056.review.cancellation.message.paragraph1.singular")
    }

    val paragraph2: String = if (multipleErrors) {
      messages("departure.ie056.review.cancellation.message.paragraph2.plural")
    } else {
      messages("departure.ie056.review.cancellation.message.paragraph2.singular")
    }

    new ReviewCancellationErrorsP5ViewModel(
      title = messages("departure.ie056.review.cancellation.message.title"),
      heading = messages("departure.ie056.review.cancellation.message.heading"),
      caption = messages("departure.messages.caption", lrn),
      paragraph1 = paragraph1,
      paragraph2 = paragraph2,
      hyperlink = messages("departure.ie056.review.cancellation.message.hyperlink"),
      functionalErrors = functionalErrors,
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfErrorsPerPage,
      href = href
    )
  }

  class ReviewCancellationErrorsP5ViewModelProvider @Inject() {

    def apply(
      functionalErrors: FunctionalErrorsWithoutSection,
      lrn: String,
      currentPage: Int,
      numberOfErrorsPerPage: Int,
      href: Call
    )(implicit messages: Messages): ReviewCancellationErrorsP5ViewModel =
      ReviewCancellationErrorsP5ViewModel(
        functionalErrors,
        lrn,
        currentPage,
        numberOfErrorsPerPage,
        href
      )
  }
}
