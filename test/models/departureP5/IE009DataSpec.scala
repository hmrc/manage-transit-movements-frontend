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
import play.api.libs.json._
import java.time.LocalDateTime

class IE009DataSpec extends SpecBase {

  "IE009Data" - {

    "must deserialize" in {

      val mrn                 = "AB123"
      val referenceNumber     = "GB00060"
      val decisionDateAndTime = LocalDateTime.now(clock)
      val decision            = "0"
      val initiatedByCustoms  = "1"
      val justification       = "justificationString"

      val json = Json.parse(s"""
          {
          "body": {
              "n1:CC009C": {
                  "TransitOperation": {
                      "MRN": "$mrn"
                  },
                  "CustomsOfficeOfDeparture": {
                      "referenceNumber": "$referenceNumber"
                  },
                  "Invalidation": {
                      "decisionDateAndTime": "$decisionDateAndTime",
                      "decision": "$decision",
                      "initiatedByCustoms": "$initiatedByCustoms",
                      "justification": "$justification"
                  }
              }
          }
          }
          """)

      val expectedResult = IE009Data(
        IE009MessageData(
          TransitOperationIE009(
            Some(mrn)
          ),
          Invalidation(
            Some(decisionDateAndTime),
            decision,
            initiatedByCustoms,
            Some(justification)
          ),
          CustomsOfficeOfDeparture(
            referenceNumber
          )
        )
      )
      val result = json.validate[IE009Data].asOpt.value
      result mustBe expectedResult
    }
  }
}
