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

package viewModels.departure

import models.departure.BusinessRejectionType.*
import play.api.i18n.Messages

case class DepartureDeclarationErrorsViewModel(lrn: String, mrn: Option[String], businessRejectionType: DepartureBusinessRejectionType) {
  def title(implicit messages: Messages): String = messages("departure.declaration.errors.message.title")

  def heading(implicit messages: Messages): String = messages("departure.declaration.errors.message.heading")

  def paragraph1(implicit messages: Messages): String =
    businessRejectionType match {
      case AmendmentRejection   => messages("departure.declaration.errors.message.amendment")
      case DeclarationRejection => messages("departure.declaration.errors.message.noerrors")
    }

  def paragraph3(implicit messages: Messages): String = messages("departure.declaration.errors.message.paragraph3")

  def hyperlink(implicit messages: Messages): Option[String] =
    businessRejectionType match {
      case AmendmentRejection   => None
      case DeclarationRejection => Some(messages("departure.declaration.errors.message.hyperlink.text"))
    }

}

object DepartureDeclarationErrorsViewModel {

  class DepartureDeclarationErrorsViewModelProvider {

    def apply(lrn: String, mrn: Option[String], businessRejectionType: DepartureBusinessRejectionType): DepartureDeclarationErrorsViewModel =
      DepartureDeclarationErrorsViewModel(lrn, mrn, businessRejectionType)

  }

}
