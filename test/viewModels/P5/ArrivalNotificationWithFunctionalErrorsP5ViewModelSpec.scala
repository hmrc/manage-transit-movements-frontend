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

package viewModels.P5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.arrivalP5.{CustomsOfficeOfDestinationActual, IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel.ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider

class ArrivalNotificationWithFunctionalErrorsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mrnString = "MRNAB123"

  "ArrivalNotificationWithFunctionalErrorsP5ViewModel" - {

    "when there is one error" - {

      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057("MRNCD3232"),
          CustomsOfficeOfDestinationActual("1234"),
          Seq(FunctionalError("14", "12", "MRN incorrect", None))
        )
      )

      val viewModelProvider = new ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data.functionalErrors, mrnString)

      "must return correct title" in {
        result.title mustBe "Review notification errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Review notification errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustBe s"There is a problem with this notification. Review the error and make a new notification with the right information."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the error (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "Make another arrival notification"
      }
    }

    "when there is multiple errors" - {
      val functionalErrors = Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))

      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057("MRNCD3232"),
          CustomsOfficeOfDestinationActual("1234"),
          functionalErrors
        )
      )

      val viewModelProvider = new ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data.functionalErrors, mrnString)

      "must return correct title" in {
        result.title mustBe "Review notification errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Review notification errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustBe s"There is a problem with this notification. Review the errors and make a new notification with the right information."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the errors (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "Make another arrival notification"
      }
    }
  }
}
