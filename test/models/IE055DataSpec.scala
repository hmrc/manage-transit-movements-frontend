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

import java.time.LocalDate

class IE055DataSpec extends SpecBase {

  "IE055Data" - {

    "must deserialize" in {

      val json: JsValue = Json.parse(
        """
          |{
          | "body": {
          |     "n1:CC055C": {
          |         "TransitOperation": {
          |             "MRN": "AB123",
          |             "declarationAcceptanceDate": "2023-09-18"
          |         },
          |         "GuaranteeReference": [
          |             {
          |                 "sequenceNumber": "token",
          |                 "GRN": "GRN1",
          |                 "InvalidGuaranteeReason": [
          |                  {
          |                      "sequenceNumber": "token",
          |                      "code": "CODE1",
          |                      "text": "TEXT1"
          |                  },
          |                  {
          |                      "sequenceNumber": "token",
          |                      "code": "CODE2",
          |                      "text": "TEXT2"
          |                  }
          |                 ]
          |             },
          |             {
          |                 "sequenceNumber": "token",
          |                 "GRN": "GRN2",
          |                 "InvalidGuaranteeReason": [
          |                  {
          |                      "sequenceNumber": "token",
          |                      "code": "CODE3"
          |                  }
          |                 ]
          |             }
          |         ]
          |     }
          | }
          |}
          |""".stripMargin
      )

      val expectedResult = IE055Data(
        IE055MessageData(
          TransitOperationIE055(
            "AB123",
            LocalDate.of(2023: Int, 9: Int, 18: Int)
          ),
          Seq(
            GuaranteeReference(
              "GRN1",
              Seq(
                InvalidGuaranteeReason(
                  "CODE1",
                  Some("TEXT1")
                ),
                InvalidGuaranteeReason(
                  "CODE2",
                  Some("TEXT2")
                )
              )
            ),
            GuaranteeReference(
              "GRN2",
              Seq(
                InvalidGuaranteeReason(
                  "CODE3",
                  None
                )
              )
            )
          )
        )
      )

      val result = json.validate[IE055Data].asOpt.value
      result mustBe expectedResult
    }
  }
}
