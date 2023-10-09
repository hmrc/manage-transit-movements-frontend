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

package helper

import base.SpecBase
import generators.Generators
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.RecoveryNotificationHelper
import viewModels.sections.Section

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecoveryNotificationHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RecoveryNotificationHelper" - {

    val recoveryNotification: RecoveryNotification =
      RecoveryNotification(LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "text", "1000", "EUR")

    val message: IE035Data = IE035Data(
      IE035MessageData(
        TransitOperationIE035(mrn, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME)),
        recoveryNotification
      )
    )

    "buildMRNRow" - {

      "must return SummaryListRow" in {

        val helper = new RecoveryNotificationHelper(message.data)

        val result = helper.buildMRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("ABCD1234567890123".toText)))
      }
    }

    "buildDeclarationAcceptanceDateRow" - {

      "must return SummaryListRow" in {

        val helper = new RecoveryNotificationHelper(message.data)

        val result = helper.buildDeclarationAcceptanceDateRow

        result mustBe
          Some(SummaryListRow(key = Key("Declaration acceptance date".toText), value = Value("09 June 2014 at 4:15 pm".toText)))
      }
    }

    "buildRecoveryDateRow" - {

      "must return SummaryListRow" in {

        val helper = new RecoveryNotificationHelper(message.data)

        val result = helper.buildRecoveryDateRow

        result mustBe
          Some(SummaryListRow(key = Key("Recovery date".toText), value = Value("09 June 2014 at 4:15 pm".toText)))
      }
    }

    "buildFurtherInformationRow" - {

      "must return SummaryListRow" in {

        val helper = new RecoveryNotificationHelper(message.data)

        val result = helper.buildFurtherInformationRow

        result mustBe
          Some(SummaryListRow(key = Key("Further information".toText), value = Value("text".toText)))
      }
    }

    "buildAmountRow" - {

      "when symbol can be found" - {
        "must return SummaryListRow" in {

          val helper = new RecoveryNotificationHelper(message.data)

          val result = helper.buildAmountRow

          result mustBe
            Some(SummaryListRow(key = Key("Amount claimed".toText), value = Value("€1000".toText)))
        }
      }

      "when symbol cannot be found" - {
        "must return SummaryListRow" in {

          val helper =
            new RecoveryNotificationHelper(message.data.copy(recoveryNotification = recoveryNotification.copy(currency = "FOO")))

          val result = helper.buildAmountRow

          result mustBe
            Some(SummaryListRow(key = Key("Amount claimed".toText), value = Value("1000 FOO".toText)))
        }
      }

    }

    "buildRecoveryNotificationSection" - {

      "must return a Section" in {

        val helper = new RecoveryNotificationHelper(message.data)

        val result = helper.buildRecoveryNotificationSection
        val firstRow =
          Seq(
            SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value(mrn.toText)),
            SummaryListRow(key = Key("Declaration acceptance date".toText), value = Value("09 June 2014 at 4:15 pm".toText)),
            SummaryListRow(key = Key("Recovery date".toText), value = Value("09 June 2014 at 4:15 pm".toText)),
            SummaryListRow(key = Key("Further information".toText), value = Value("text".toText)),
            SummaryListRow(key = Key("Amount claimed".toText), value = Value("€1000".toText))
          )

        result mustBe Section(None, firstRow, None)

      }
    }
  }

}
