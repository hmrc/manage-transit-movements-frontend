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
import models.FunctionalError.FunctionalErrorWithSection
import models.FunctionalErrors.FunctionalErrorsWithSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.ErrorPaginationViewModel

case class ReviewDepartureAmendmentErrorsP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1: String,
  paragraph2: String,
  hyperlink: Option[String],
  functionalErrors: FunctionalErrorsWithSection,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  departureId: String,
  messageId: String
) extends ErrorPaginationViewModel[FunctionalErrorWithSection, FunctionalErrorsWithSection] {

  override def href(page: Int): Call =
    routes.ReviewDepartureAmendmentErrorsP5Controller.onPageLoad(Some(page), departureId, messageId)
}

object ReviewDepartureAmendmentErrorsP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithSection,
    lrn: String,
    currentPage: Option[Int],
    numberOfErrorsPerPage: Int,
    departureId: String,
    messageId: String
  )(implicit messages: Messages): ReviewDepartureAmendmentErrorsP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val heading: String = messages("departure.ie022.review.message.heading")

    val paragraph1: String =
      if (multipleErrors) {
        messages("departure.ie022.review.message.paragraph1.amendment.plural")
      } else {
        messages("departure.ie022.review.message.paragraph1.amendment.singular")
      }

    val paragraph2: String = if (multipleErrors) {
      messages("departure.ie022.review.message.paragraph2.plural")
    } else {
      messages("departure.ie022.review.message.paragraph2.singular")
    }

    new ReviewDepartureAmendmentErrorsP5ViewModel(
      title = messages("departure.ie022.review.message.title"),
      heading = heading,
      caption = messages("departure.messages.caption", lrn),
      paragraph1 = paragraph1,
      paragraph2 = paragraph2,
      hyperlink = None,
      functionalErrors = functionalErrors,
      currentPage = currentPage.getOrElse(1),
      numberOfItemsPerPage = numberOfErrorsPerPage,
      departureId = departureId,
      messageId = messageId
    )
  }
}
