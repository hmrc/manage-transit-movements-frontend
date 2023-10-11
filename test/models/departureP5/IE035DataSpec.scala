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

import java.time.LocalDate

class IE035DataSpec extends SpecBase {

  "IE035Data" - {

    "must deserialize" in {

      val mrn                       = "AB123"
      val declarationAcceptanceDate = LocalDate.now(clock)
      val recoveryNotificationDate  = LocalDate.now(clock)
      val recoveryNotificationText  = "text"
      val amountClaimed: Double     = 1000.01
      val currency                  = "EUR"

      val json = Json.parse(s"""
          {
          "body": {
              "n1:CC035C": {
                  "TransitOperation": {
                      "MRN": "$mrn",
                      "declarationAcceptanceDate": "$declarationAcceptanceDate"
                  },
                  "RecoveryNotification": {
                      "recoveryNotificationDate": "$recoveryNotificationDate",
                      "recoveryNotificationText": "$recoveryNotificationText",
                      "amountClaimed": $amountClaimed,
                      "currency": "$currency"
                  }
              }
          }
          }
          """)

      val expectedResult = IE035Data(
        IE035MessageData(
          TransitOperationIE035(
            mrn,
            declarationAcceptanceDate
          ),
          RecoveryNotification(
            recoveryNotificationDate,
            recoveryNotificationText,
            amountClaimed.toString,
            currency
          )
        )
      )
      val result = json.validate[IE035Data].asOpt.value
      result mustBe expectedResult
    }
  }
}
