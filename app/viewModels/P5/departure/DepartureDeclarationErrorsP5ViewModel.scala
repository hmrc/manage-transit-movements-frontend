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

import play.api.i18n.Messages

case class DepartureDeclarationErrorsP5ViewModel(lrn: String, noErrors: Boolean) {
  def title(implicit messages: Messages): String = messages("departure.declaration.errors.message.title")

  def heading(implicit messages: Messages): String = messages("departure.declaration.errors.message.heading")

  def paragraph1(implicit messages: Messages): String =
    if (noErrors) {
      messages("departure.declaration.errors.message.noerrors", lrn)
    } else {
      messages("departure.declaration.errors.message.elevenpluserrors", lrn)
    }

  def paragraph2(implicit messages: Messages): String = messages("departure.declaration.errors.message.paragraph2")

  def paragraph3Prefix(implicit messages: Messages): String = messages("departure.declaration.errors.message.paragraph3.prefix")
  def paragraph3Suffix(implicit messages: Messages): String = messages("departure.declaration.errors.message.paragraph3.suffix")
  def paragraph3Link(implicit messages: Messages): String   = messages("departure.declaration.errors.message.paragraph3.link")

  def hyperlink(implicit messages: Messages): String = messages("departure.declaration.errors.message.hyperlink")

}

object DepartureDeclarationErrorsP5ViewModel {

  class DepartureDeclarationErrorsP5ViewModelProvider {
    def apply(lrn: String, noErrors: Boolean): DepartureDeclarationErrorsP5ViewModel = DepartureDeclarationErrorsP5ViewModel(lrn, noErrors)

  }
}
