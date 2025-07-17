/*
 * Copyright 2024 HM Revenue & Customs
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
import models.departureP5.BusinessRejectionType.*
import models.departureP5.Rejection.*
import play.api.libs.json.Json

class RejectionSpec extends SpecBase {

  "Rejection" - {
    "when IE055Rejection" - {
      "must serialise" in {
        val rejection = IE055Rejection(
          departureId = departureIdP5
        )

        val expectedResult = Json.parse(s"""
            |{
            |  "type" : "IE055",
            |  "departureId" : "$departureIdP5"
            |}
            |""".stripMargin)

        val result = Json.toJson(rejection)

        result.mustEqual(expectedResult)
      }
    }

    "when IE056Rejection" - {
      "must serialise" in {
        val rejection = IE056Rejection(
          departureId = departureIdP5,
          businessRejectionType = DeclarationRejection,
          errorPointers = Seq(
            "foo",
            "bar"
          )
        )

        val expectedResult = Json.parse(s"""
             |{
             |  "type" : "IE056",
             |  "departureId" : "$departureIdP5",
             |  "businessRejectionType" : "015",
             |  "errorPointers" : [
             |    "foo",
             |    "bar"
             |  ]
             |}
             |""".stripMargin)

        val result = Json.toJson(rejection)

        result.mustEqual(expectedResult)
      }
    }
  }
}
