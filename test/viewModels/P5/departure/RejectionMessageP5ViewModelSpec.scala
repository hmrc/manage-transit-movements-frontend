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

import base.SpecBase
import generators.Generators
import models.FunctionalError.FunctionalErrorWithSection
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.InvalidDataItem
import models.departureP5.BusinessRejectionType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Call
import play.api.test.Helpers.GET

class RejectionMessageP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val lrnString  = "LRNAB123"
  private val href: Call = Call(GET, "#")

  "RejectionMessageP5ViewModel" - {

    "when there is one error" - {
      val errors = FunctionalErrorsWithSection(
        Seq(
          FunctionalErrorWithSection(
            error = "error",
            businessRuleId = "business rule ID",
            section = Some("Trader details"),
            invalidDataItem = new InvalidDataItem("invalid data item"),
            invalidAnswer = Some("invalid answer")
          )
        )
      )

      "when 015 rejection type" - {

        val result = RejectionMessageP5ViewModel(errors, lrnString, BusinessRejectionType.DeclarationRejection, None, 20, href)

        "must return correct title" in {
          result.title mustEqual "Amend declaration errors"
        }
        "must return correct heading" in {
          result.heading mustEqual "Amend declaration errors"
        }
        "must return correct paragraph 1" in {
          result.paragraph1 mustEqual s"There is a problem with this declaration. Amend the error and resend the declaration."
        }

        "must return correct paragraph 2" in {
          result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)"
        }
        "must return correct hyperlink text" in {
          result.hyperlink.value mustEqual "Make another departure declaration"
        }
      }

      "when 013 rejection type" - {

        val result = RejectionMessageP5ViewModel(errors, lrnString, BusinessRejectionType.AmendmentRejection, None, 20, href)

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
        "must return correct hyperlink text" in {
          result.hyperlink mustEqual None
        }
      }
    }

    "when there are multiple errors" - {

      val errors = FunctionalErrorsWithSection(
        Seq(
          FunctionalErrorWithSection(
            error = "error 1",
            businessRuleId = "business rule ID 1",
            section = Some("Trader details"),
            invalidDataItem = new InvalidDataItem("invalid data item 1"),
            invalidAnswer = Some("invalid answer 1")
          ),
          FunctionalErrorWithSection(
            error = "error 2",
            businessRuleId = "business rule ID 2",
            section = Some("Trader details"),
            invalidDataItem = new InvalidDataItem("invalid data item 2"),
            invalidAnswer = Some("invalid answer 2")
          )
        )
      )

      "when 015 rejection type" - {

        val result = RejectionMessageP5ViewModel(errors, lrnString, BusinessRejectionType.DeclarationRejection, None, 20, href)

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
        "must return correct hyperlink text" in {
          result.hyperlink.value mustEqual "Make another departure declaration"
        }
      }

      "when 013 rejection type" - {

        val result = RejectionMessageP5ViewModel(errors, lrnString, BusinessRejectionType.AmendmentRejection, None, 20, href)

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
        "must return correct hyperlink text" in {
          result.hyperlink mustEqual None
        }
      }
    }
  }
}
