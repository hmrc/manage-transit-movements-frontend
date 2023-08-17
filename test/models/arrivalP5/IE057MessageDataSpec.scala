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

package models.arrivalP5

import base.SpecBase
import models.departureP5.FunctionalError
import play.api.inject.guice.GuiceApplicationBuilder

class IE057MessageDataSpec extends SpecBase {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(conf = "pagination.arrival.numberOfErrorsPerPage" -> 2)

  "IE057MessageData" - {

    val ie057: IE057MessageData = IE057MessageData(
      TransitOperationIE057("AB123"),
      CustomsOfficeOfDestinationActual("1234"),
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

            val ie057WithErrors: IE057MessageData = ie057.copy(
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

            val result = ie057WithErrors.pagedFunctionalErrors(1)(paginationAppConfig)

            result mustBe expectedResult
          }

          "and there are errors that do not exceed the total number of errors per page" in {

            val ie057WithErrors: IE057MessageData = ie057.copy(functionalErrors = Seq(functionalError1))

            val expectedResult = Seq(functionalError1)

            val result = ie057WithErrors.pagedFunctionalErrors(1)(paginationAppConfig)

            result mustBe expectedResult
          }
        }

        "when on second index" - {

          "and there are multiple errors that exceeds the total number of errors per page" in {

            val ie057WithErrors: IE057MessageData = ie057.copy(
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

            val result = ie057WithErrors.pagedFunctionalErrors(2)(paginationAppConfig)

            result mustBe expectedResult
          }

          "and there are errors that do not exceed the total number of errors per page" in {

            val ie057WithErrors: IE057MessageData = ie057.copy(
              functionalErrors = Seq(
                functionalError1,
                functionalError3,
                functionalError2
              )
            )
            val expectedResult = Seq(functionalError3)

            val result = ie057WithErrors.pagedFunctionalErrors(2)(paginationAppConfig)

            result mustBe expectedResult
          }
        }
      }
    }
  }

}
