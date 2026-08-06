/*
 * Copyright 2026 HM Revenue & Customs
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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.AmendDeclarationErrorsViewModel.AmendDeclarationErrorsViewModelProvider

class AmendDeclarationErrorsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val lrnString = nonEmptyString.sample.value
  private val mrnString = Gen.option(nonEmptyString).sample.value

  "AmendDeclarationErrorsViewModel" - {

    val viewModelProvider = new AmendDeclarationErrorsViewModelProvider()
    val result            = viewModelProvider.apply(lrnString, mrnString)

    "must return correct title" in {
      result.title mustEqual "Declaration errors"
    }

    "must return correct heading" in {
      result.heading mustEqual "Declaration errors"
    }

    "must return correct paragraph1" in {
      result.paragraph1 mustEqual "There are one or more errors in this declaration that cannot be amended. Make a new declaration with the right information."
    }

    "must return correct paragraph2" in {
      result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
    }

    "must return correct hyperlink text" in {
      result.hyperlink.value mustEqual "Make another departure declaration"
    }

    "must return the provided lrn" in {
      result.lrn mustEqual lrnString
    }

    "must return the provided mrn" in {
      result.mrn mustEqual mrnString
    }
  }

}
