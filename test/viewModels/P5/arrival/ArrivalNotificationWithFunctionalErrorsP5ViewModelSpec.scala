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

import base.SpecBase
import generators.Generators
import models.FunctionalError.FunctionalErrorWithoutSection
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import models.InvalidDataItem
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Call
import play.api.test.Helpers.GET

class ArrivalNotificationWithFunctionalErrorsP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mrnString  = "MRNAB123"
  private val href: Call = Call(GET, "#")

  "ArrivalNotificationWithFunctionalErrorsP5ViewModel" - {

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

      val result = ArrivalNotificationWithFunctionalErrorsP5ViewModel(errors, mrnString, None, 20, href)

      "must return correct section length" in {
        result.pagination.items.value.length mustEqual 1
      }
      "must return correct title" in {
        result.title mustEqual "Review notification errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Review notification errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual "There is a problem with this notification. Review the error and make a new notification with the right information."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "We will keep your previous answers for 30 days - so if you use the same MRN within this time, your answers will be pre-populated."
      }
      "must return correct paragraph 3" in {
        result.paragraph3 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustEqual "Make another arrival notification"
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

      val result = ArrivalNotificationWithFunctionalErrorsP5ViewModel(errors, mrnString, None, 20, href)

      "must return correct section length" in {
        result.pagination.items.value.length mustEqual 2
      }
      "must return correct title" in {
        result.title mustEqual "Review notification errors"
      }
      "must return correct heading" in {
        result.heading mustEqual "Review notification errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustEqual "There is a problem with this notification. Review the errors and make a new notification with the right information."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustEqual "We will keep your previous answers for 30 days - so if you use the same MRN within this time, your answers will be pre-populated."
      }
      "must return correct paragraph 3" in {
        result.paragraph3 mustEqual "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustEqual "Make another arrival notification"
      }
    }
  }

}
