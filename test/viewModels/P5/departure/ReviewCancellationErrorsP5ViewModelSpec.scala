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
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import models.InvalidDataItem
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ReviewCancellationErrorsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val lrnString = "LRNAB123"

  "ReviewCancellationErrorsP5ViewModel" - {

    "when there is one error" - {
      val errors = FunctionalErrorsWithoutSection(
        Seq(
          FunctionalErrorWithoutSection(
            error = "error",
            businessRuleId = "business rule ID",
            invalidDataItem = new InvalidDataItem("invalid data item"),
            invalidAnswer = Some("invalid answer")
          )
        )
      )

      val result = ReviewCancellationErrorsP5ViewModel(errors, lrnString, None, 20, departureIdP5, messageId)

      "must return correct title" in {
        result.title mustEqual "Review cancellation errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Review cancellation errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual s"The office of departure was not able to cancel this declaration. Review the error - then if you still want to cancel the declaration, try cancelling it again."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustEqual "View departure declarations"
      }

      "must return correct url" in {
        result.viewDeparturesLink mustEqual controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
      }
    }

    "when there is multiple errors" - {
      val errors = FunctionalErrorsWithoutSection(
        Seq(
          FunctionalErrorWithoutSection(
            error = "error 1",
            businessRuleId = "business rule ID 1",
            invalidDataItem = new InvalidDataItem("invalid data item 1"),
            invalidAnswer = Some("invalid answer 1")
          ),
          FunctionalErrorWithoutSection(
            error = "error 2",
            businessRuleId = "business rule ID 2",
            invalidDataItem = new InvalidDataItem("invalid data item 2"),
            invalidAnswer = Some("invalid answer 2")
          )
        )
      )

      val result = ReviewCancellationErrorsP5ViewModel(errors, lrnString, None, 20, departureIdP5, messageId)

      "must return correct title" in {
        result.title mustEqual "Review cancellation errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Review cancellation errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual s"The office of departure was not able to cancel this declaration. Review the errors - then if you still want to cancel the declaration, try cancelling it again."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustEqual "View departure declarations"
      }
      "must return correct url" in {
        result.viewDeparturesLink mustEqual controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
      }
    }
  }

}
