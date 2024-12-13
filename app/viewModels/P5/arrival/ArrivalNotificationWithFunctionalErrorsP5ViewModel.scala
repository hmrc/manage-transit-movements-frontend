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

package viewModels.P5.arrival

import controllers.arrivalP5.routes
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.ErrorPaginationViewModel

case class ArrivalNotificationWithFunctionalErrorsP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1: String,
  paragraph2: String,
  paragraph3: String,
  hyperlink: String,
  functionalErrors: FunctionalErrorsWithoutSection,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  arrivalId: String,
  messageId: String
) extends ErrorPaginationViewModel[FunctionalErrorWithoutSection, FunctionalErrorsWithoutSection] {

  override def href(page: Int): Call =
    routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(Some(page), arrivalId, messageId)
}

object ArrivalNotificationWithFunctionalErrorsP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithoutSection,
    mrn: String,
    currentPage: Option[Int],
    numberOfErrorsPerPage: Int,
    arrivalId: String,
    messageId: String
  )(implicit messages: Messages): ArrivalNotificationWithFunctionalErrorsP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val paragraph1: String = if (multipleErrors) {
      messages("arrival.ie057.review.notification.message.paragraph1.plural")
    } else {
      messages("arrival.ie057.review.notification.message.paragraph1.singular")
    }

    val paragraph3: String = if (multipleErrors) {
      messages("arrival.ie057.review.notification.message.paragraph3.plural")
    } else {
      messages("arrival.ie057.review.notification.message.paragraph3.singular")
    }

    new ArrivalNotificationWithFunctionalErrorsP5ViewModel(
      title = messages("arrival.ie057.review.notification.message.title"),
      heading = messages("arrival.ie057.review.notification.message.heading"),
      caption = messages("arrival.messages.caption", mrn),
      paragraph1 = paragraph1,
      paragraph2 = messages("arrival.ie057.review.notification.message.paragraph2"),
      paragraph3 = paragraph3,
      hyperlink = messages("arrival.ie057.review.notification.message.hyperlink"),
      functionalErrors = functionalErrors,
      currentPage = currentPage.getOrElse(1),
      numberOfItemsPerPage = numberOfErrorsPerPage,
      arrivalId = arrivalId,
      messageId = messageId
    )
  }
}
