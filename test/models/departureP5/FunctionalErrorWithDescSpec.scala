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
import play.api.libs.json.Json

class FunctionalErrorWithDescSpec extends SpecBase {

  "FunctionalError" - {

    "must deserialise" - {
      "when there are no functional errors" in {
        val json = Json.parse("""
            |[]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Nil
      }

      "when there is one functional error" in {
        val json = Json.parse("""
            |[
            |    {
            |        "errorPointer": "/CC014C",
            |        "errorCode": "12",
            |        "errorReason": "N/A"
            |    }
            |]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Seq(
          FunctionalError("/CC014C", "12", "N/A")
        )
      }

      "when there are multiple functional errors" in {
        val json = Json.parse("""
            |[
            |    {
            |        "errorPointer": "/CC014C",
            |        "errorCode": "12",
            |        "errorReason": "N/A"
            |    },
            |    {
            |        "errorPointer": "/CC015C",
            |        "errorCode": "13",
            |        "errorReason": "Value too long"
            |    }
            |]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Seq(
          FunctionalError("/CC014C", "12", "N/A"),
          FunctionalError("/CC015C", "13", "Value too long")
        )
      }
    }
  }

}
