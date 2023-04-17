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

import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.{alphaNumericRegex, alphaNumericRegexHyphensUnderscores}
import org.scalacheck.Gen
import play.api.data.FormError

class SearchFormProviderSpec extends StringFieldBehaviours {

  ".value departures" - {

    val form = new SearchFormProvider()("departures.search.form.value.invalid", alphaNumericRegexHyphensUnderscores)

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.alphaNumStr
    )

    behave like allowsHyphensAndUnderscores(
      form,
      fieldName
    )

    behave like nonMandatoryField(
      form,
      fieldName
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, "departures.search.form.value.invalid", Seq(alphaNumericRegexHyphensUnderscores.regex))
    )
  }

  ".value arrivals" - {

    val form = new SearchFormProvider()("arrivals.search.form.value.invalid")

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.alphaNumStr
    )

    behave like nonMandatoryField(
      form,
      fieldName
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, "arrivals.search.form.value.invalid", Seq(alphaNumericRegex.regex))
    )
  }
}
