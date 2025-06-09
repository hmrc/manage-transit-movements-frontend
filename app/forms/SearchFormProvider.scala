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

package forms

import forms.mappings.Mappings
import models.domain.StringFieldRegex.{alphaNumericRegex, alphaNumericRegexHyphensUnderscores}
import play.api.data.Form
import play.api.data.Forms.optional

import scala.util.matching.Regex

trait SearchFormProvider extends Mappings {

  val prefix: String

  val regex: Regex

  def apply(): Form[Option[String]] =
    Form(
      "value" -> optional(
        text()
          .verifying(regexp(regex.toString(), s"$prefix.search.form.value.invalid"))
      )
    )

}

class DeparturesSearchFormProvider extends SearchFormProvider {
  override val prefix: String = "departures"
  override val regex: Regex   = alphaNumericRegexHyphensUnderscores
}

class ArrivalsSearchFormProvider extends SearchFormProvider {
  override val prefix: String = "arrivals"
  override val regex: Regex   = alphaNumericRegex
}
