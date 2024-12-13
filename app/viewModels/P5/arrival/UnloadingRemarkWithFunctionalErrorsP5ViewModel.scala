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

case class UnloadingRemarkWithFunctionalErrorsP5ViewModel(
  title: String,
  heading: String,
  caption: String,
  paragraph1: String,
  paragraph2: String,
  hyperlink: String,
  functionalErrors: FunctionalErrorsWithoutSection,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  arrivalId: String,
  messageId: String
) extends ErrorPaginationViewModel[FunctionalErrorWithoutSection, FunctionalErrorsWithoutSection] {

  override def href(page: Int): Call =
    routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(Some(page), arrivalId, messageId)
}

object UnloadingRemarkWithFunctionalErrorsP5ViewModel {

  def apply(
    functionalErrors: FunctionalErrorsWithoutSection,
    mrn: String,
    currentPage: Option[Int],
    numberOfErrorsPerPage: Int,
    arrivalId: String,
    messageId: String
  )(implicit messages: Messages): UnloadingRemarkWithFunctionalErrorsP5ViewModel = {

    val multipleErrors: Boolean = functionalErrors.multipleErrors

    val paragraph1: String = if (multipleErrors) {
      messages("arrival.ie057.review.unloading.message.paragraph1.plural")
    } else {
      messages("arrival.ie057.review.unloading.message.paragraph1.singular")
    }

    val paragraph2: String = if (multipleErrors) {
      messages("arrival.ie057.review.unloading.message.paragraph2.plural")
    } else {
      messages("arrival.ie057.review.unloading.message.paragraph2.singular")
    }

    new UnloadingRemarkWithFunctionalErrorsP5ViewModel(
      title = messages("arrival.ie057.review.unloading.message.title"),
      heading = messages("arrival.ie057.review.unloading.message.heading", mrn),
      caption = messages("arrival.messages.caption", mrn),
      paragraph1 = paragraph1,
      paragraph2 = paragraph2,
      hyperlink = messages("arrival.ie057.review.unloading.message.hyperlink"),
      functionalErrors = functionalErrors,
      currentPage = currentPage.getOrElse(1),
      numberOfItemsPerPage = numberOfErrorsPerPage,
      arrivalId = arrivalId,
      messageId = messageId
    )
  }
}
