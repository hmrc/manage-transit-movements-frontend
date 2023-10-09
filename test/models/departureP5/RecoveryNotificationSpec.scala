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

import java.time.LocalDateTime

class RecoveryNotificationSpec extends SpecBase {

  "RecoveryNotification" - {

    val recoveryNotification = RecoveryNotification(LocalDateTime.now(clock), "text", "1000", "EUR")

    "currencySymbol" - {

      "must convert currency to a symbol" - {
        "when EUR must return €" in {

          recoveryNotification.formattedCurrency mustBe "€1000"
        }

        "when GBP must return £" in {
          val updatedRecoveryNotification = recoveryNotification.copy(currency = "GBP")
          updatedRecoveryNotification.formattedCurrency mustBe "£1000"
        }

        "when unknown must return code" in {
          val updatedRecoveryNotification = recoveryNotification.copy(currency = "FOO")
          updatedRecoveryNotification.formattedCurrency mustBe "1000 FOO"
        }
      }
    }
  }

}
