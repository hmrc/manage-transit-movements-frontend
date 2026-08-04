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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.FunctionalError.FunctionalErrorWithSection
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.InvalidDataItem
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DeclarationAmendmentRejectionMessageViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val lrnString = "LRNAB123"

  "DeclarationAmendmentRejectionMessageViewModel" - {

    "when there is one error" - {
      val errors = FunctionalErrorsWithSection(
        Seq(
          FunctionalErrorWithSection(
            error = "error",
            businessRuleId = Some("business rule ID"),
            section = Some("Documents"),
            invalidDataItem = Some(new InvalidDataItem("invalid data item")),
            invalidAnswer = Some("invalid answer")
          )
        )
      )

      val result = DeclarationAmendmentRejectionMessageViewModel(errors, lrnString, None, 20, departureIdP5, messageId)

      "must return correct title" in {
        result.title mustEqual "Amend declaration errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Amend declaration errors"
      }
      "must return correct paragraph 1 prefix" in {
        result.paragraph1Prefix mustEqual s"There is a problem with departure declaration $lrnString."
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual s"There is a problem with this declaration. Amend the error and resend the declaration."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)"
      }

    }

    "when there are multiple errors" - {

      val errors = FunctionalErrorsWithSection(
        Seq(
          FunctionalErrorWithSection(
            error = "error 1",
            businessRuleId = Some("business rule ID 1"),
            section = Some("Documents"),
            invalidDataItem = Some(new InvalidDataItem("invalid data item 1")),
            invalidAnswer = Some("invalid answer 1")
          ),
          FunctionalErrorWithSection(
            error = "error 2",
            businessRuleId = Some("business rule ID 2"),
            section = Some("Documents"),
            invalidDataItem = Some(new InvalidDataItem("invalid data item 2")),
            invalidAnswer = Some("invalid answer 2")
          )
        )
      )

      val result = DeclarationAmendmentRejectionMessageViewModel(errors, lrnString, None, 20, departureIdP5, messageId)

      "must return correct title" in {
        result.title mustEqual "Amend declaration errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Amend declaration errors"
      }
      "must return correct paragraph 1 prefix" in {
        result.paragraph1Prefix mustEqual s"There is a problem with departure declaration $lrnString."
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual s"There is a problem with this declaration. Amend the errors and resend the declaration."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
      }

    }
  }
}
