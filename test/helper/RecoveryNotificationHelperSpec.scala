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
import generated.CC035CType
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scalaxb.XMLCalendar
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.RecoveryNotificationHelper
import viewModels.sections.Section

class RecoveryNotificationHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val amountClaimed: BigDecimal = 1000

  "RecoveryNotificationHelper" - {

    "buildMRNRow" - {

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          mrn =>
            forAll(arbitrary[CC035CType].map {
              x =>
                x.copy(TransitOperation = x.TransitOperation.copy(MRN = mrn))
            }) {
              message =>
                val helper = new RecoveryNotificationHelper(message)

                val result = helper.buildMRNRow

                result mustBe
                  Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value(mrn.toText)))
            }
        }
      }
    }

    "buildDeclarationAcceptanceDateRow" - {

      "must return SummaryListRow" in {
        val declarationAcceptanceDate = XMLCalendar("2014-06-09T16:15:04+01:00")

        forAll(arbitrary[CC035CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(declarationAcceptanceDate = declarationAcceptanceDate))
        }) {
          message =>
            val helper = new RecoveryNotificationHelper(message)

            val result = helper.buildDeclarationAcceptanceDateRow

            result mustBe
              Some(SummaryListRow(key = Key("Declaration acceptance date".toText), value = Value("09 June 2014".toText)))
        }
      }
    }

    "buildRecoveryDateRow" - {

      "must return SummaryListRow" in {
        val recoveryNotificationDate = XMLCalendar("2014-06-09T16:15:04+01:00")

        forAll(arbitrary[CC035CType].map {
          x =>
            x.copy(RecoveryNotification = x.RecoveryNotification.copy(recoveryNotificationDate = Some(recoveryNotificationDate)))
        }) {
          message =>
            val helper = new RecoveryNotificationHelper(message)

            val result = helper.buildRecoveryDateRow

            result mustBe
              Some(SummaryListRow(key = Key("Recovery date".toText), value = Value("09 June 2014".toText)))
        }
      }
    }

    "buildFurtherInformationRow" - {

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          recoveryNotificationText =>
            forAll(arbitrary[CC035CType].map {
              x =>
                x.copy(RecoveryNotification = x.RecoveryNotification.copy(recoveryNotificationText = Some(recoveryNotificationText)))
            }) {
              message =>
                val helper = new RecoveryNotificationHelper(message)

                val result = helper.buildFurtherInformationRow

                result mustBe
                  Some(SummaryListRow(key = Key("Further information".toText), value = Value(recoveryNotificationText.toText)))
            }
        }
      }
    }

    "buildAmountRow" - {
      "when symbol can be found" - {
        "must return SummaryListRow" in {
          forAll(arbitrary[CC035CType].map {
            x =>
              x.copy(RecoveryNotification = x.RecoveryNotification.copy(amountClaimed = amountClaimed))
              x.copy(RecoveryNotification = x.RecoveryNotification.copy(currency = "EUR"))
          }) {
            message =>
              val helper = new RecoveryNotificationHelper(message)

              val result = helper.buildAmountRow

              result mustBe
                Some(SummaryListRow(key = Key("Amount claimed".toText), value = Value("€1000".toText)))
          }
        }
      }

      "when symbol cannot be found" - {
        "must return SummaryListRow" in {
          forAll(arbitrary[CC035CType].map {
            x =>
              x.copy(RecoveryNotification = x.RecoveryNotification.copy(amountClaimed = amountClaimed))
              x.copy(RecoveryNotification = x.RecoveryNotification.copy(currency = "FOO"))
          }) {
            message =>
              val helper = new RecoveryNotificationHelper(message)

              val result = helper.buildAmountRow

              result mustBe
                Some(SummaryListRow(key = Key("Amount claimed".toText), value = Value("1000 FOO".toText)))
          }
        }
      }
    }

    "buildRecoveryNotificationSection" - {

      "must return a Section" in {
        forAll(arbitrary[CC035CType].map {
          x =>
            x
              .copy(TransitOperation = x.TransitOperation.copy(MRN = "MRN1"))
              .copy(TransitOperation = x.TransitOperation.copy(declarationAcceptanceDate = XMLCalendar("2014-06-09T16:15:04+01:00")))
              .copy(RecoveryNotification = x.RecoveryNotification.copy(recoveryNotificationDate = Some(XMLCalendar("2014-06-09T16:15:04+01:00"))))
              .copy(RecoveryNotification = x.RecoveryNotification.copy(amountClaimed = amountClaimed))
              .copy(RecoveryNotification = x.RecoveryNotification.copy(currency = "EUR"))
        }) {
          message =>
            val helper = new RecoveryNotificationHelper(message)

            val result = helper.buildRecoveryNotificationSection
            val firstRow =
              Seq(
                SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value(mrn.toText)),
                SummaryListRow(key = Key("Declaration acceptance date".toText), value = Value("09 June 2014".toText)),
                SummaryListRow(key = Key("Recovery date".toText), value = Value("09 June 2014".toText)),
                SummaryListRow(key = Key("Further information".toText), value = Value("text".toText)),
                SummaryListRow(key = Key("Amount claimed".toText), value = Value("€1000".toText))
              )

            result mustBe Section(None, firstRow, None)
        }
      }
    }
  }
}
