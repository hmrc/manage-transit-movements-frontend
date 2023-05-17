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
import play.api.libs.json._

class IE057DataSpec extends SpecBase {

  "IE057Data" - {

    "must deserialize" in {

      val json = Json.parse("""
          |{
          | "body": {
          |   "n1:CC057C": {
          |     "TransitOperation": {
          |       "MRN": "AB123"
          |     },
          |     "FunctionalError": [
          |     {
          |       "errorPointer": "1",
          |       "errorCode": "12",
          |       "errorReason": "Codelist violation"
          |     },
          |     {
          |       "errorPointer": "2",
          |       "errorCode": "14",
          |       "errorReason": "Rule violation",
          |       "originalAttributeValue": "test"
          |     }
          |    ]
          |   }
          | }
          |}
          |""".stripMargin)

      val expectedResult = IE057Data(
        IE057MessageData(
          TransitOperationIE057(
            "AB123"
          ),
          Seq(
            FunctionalError("1", "12", "Codelist violation", None),
            FunctionalError("2", "14", "Rule violation", Some("test"))
          )
        )
      )

      val result: IE057Data = json.validate[IE057Data].asOpt.value

      result mustBe expectedResult
    }
  }
}
