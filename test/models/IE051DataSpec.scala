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

package models

import base.SpecBase
import models.departureP5._
import play.api.libs.json._

import java.time.LocalDateTime

class IE051DataSpec extends SpecBase {

  "IE051Data" - {

    "must deserialize" in {

      val declarationSubmissionDateAndTime = LocalDateTime.now()

      val json: JsValue = Json.parse(
        s"""
          |{
          | "body": {
          |     "n1:CC051C": {
          |         "TransitOperation": {
          |             "MRN": "AB123",
          |             "declarationSubmissionDateAndTime": "$declarationSubmissionDateAndTime",
          |             "noReleaseMotivationCode": "G1",
          |             "noReleaseMotivationText": "Guarantee not valid"
          |         }
          |     }
          | }
          |}
          |""".stripMargin
      )

      val expectedResult = IE051Data(
        IE051MessageData(
          TransitOperationIE051(
            "AB123",
            declarationSubmissionDateAndTime,
            "G1",
            "Guarantee not valid"
          )
        )
      )

      val result = json.validate[IE051Data].asOpt.value
      result mustBe expectedResult
    }
  }
}
