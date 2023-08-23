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

package models.departureP5

import base.SpecBase
import models.RejectionType
import play.api.inject.guice.GuiceApplicationBuilder

class IE056MessageDataSpec extends SpecBase {

  private val rejectionType: RejectionType = RejectionType.DeclarationRejection

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(conf = "pagination.departure.numberOfErrorsPerPage" -> 2)

  "IE056MessageData" - {

    val ie056: IE056MessageData = IE056MessageData(
      TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), rejectionType),
      CustomsOfficeOfDeparture("AB123"),
      Seq.empty
    )

    val functionalError1 = FunctionalError("foo/bar", "1", "reason", None)
    val functionalError2 = FunctionalError("foo/bar", "2", "reason", None)
    val functionalError3 = FunctionalError("foo/bar", "3", "reason", None)
    val functionalError4 = FunctionalError("foo/bar", "4", "reason", None)
    val functionalError5 = FunctionalError("foo/bar", "5", "reason", None)

    "pagedFunctionalErrors" - {

      "must sort and slice" - {

        "when on first index" - {

          "and there are multiple errors that exceeds the total number of errors per page" in {

            val ie056WithErrors = ie056.copy(
              functionalErrors = Seq(
                functionalError1,
                functionalError3,
                functionalError2
              )
            )

            val expectedResult = Seq(
              functionalError1,
              functionalError2
            )

            val result = ie056WithErrors.pagedFunctionalErrors(1)(paginationAppConfig)

            result mustBe expectedResult
          }

          "and there are errors that do not exceed the total number of errors per page" in {

            val ie056WithErrors = ie056.copy(functionalErrors = Seq(functionalError1))

            val expectedResult = Seq(functionalError1)

            val result = ie056WithErrors.pagedFunctionalErrors(1)(paginationAppConfig)

            result mustBe expectedResult
          }
        }

        "when on second index" - {

          "and there are multiple errors that exceeds the total number of errors per page" in {

            val ie056WithErrors = ie056.copy(
              functionalErrors = Seq(
                functionalError1,
                functionalError3,
                functionalError2,
                functionalError5,
                functionalError4
              )
            )

            val expectedResult = Seq(
              functionalError3,
              functionalError4
            )

            val result = ie056WithErrors.pagedFunctionalErrors(2)(paginationAppConfig)

            result mustBe expectedResult
          }

          "and there are errors that do not exceed the total number of errors per page" in {

            val ie056WithErrors = ie056.copy(
              functionalErrors = Seq(
                functionalError1,
                functionalError3,
                functionalError2
              )
            )
            val expectedResult = Seq(functionalError3)

            val result = ie056WithErrors.pagedFunctionalErrors(2)(paginationAppConfig)

            result mustBe expectedResult
          }
        }
      }
    }
  }

}
