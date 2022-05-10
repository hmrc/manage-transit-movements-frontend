/*
 * Copyright 2022 HM Revenue & Customs
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

package views.utils

import base.SpecBase
import generators.Generators
import models.FunctionalError
import models.departure.{NoReleaseForTransitMessage, ResultsOfControl}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import views.utils.ViewUtils._

class ViewUtilsSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RichFunctionalError" - {
    ".toSummaryList" - {
      "must return summary list" - {
        "when reason nor original attribute value defined" in {
          forAll(arbitrary[FunctionalError]) {
            functionalError =>
              val result = functionalError
                .copy(
                  reason = None,
                  originalAttributeValue = None
                )
                .toSummaryList

              result.rows.length mustBe 2

              result.rows.head.key.content mustBe "Error type".toText
              result.rows.head.value.content mustBe functionalError.errorType.toString.toText

              result.rows(1).key.content mustBe "Error pointer".toText
              result.rows(1).value.content mustBe functionalError.pointer.value.toText
          }
        }

        "when reason defined" in {
          forAll(arbitrary[FunctionalError], Gen.alphaNumStr) {
            (functionalError, reason) =>
              val result = functionalError
                .copy(
                  reason = Some(reason),
                  originalAttributeValue = None
                )
                .toSummaryList

              result.rows.length mustBe 3

              result.rows.head.key.content mustBe "Error type".toText
              result.rows.head.value.content mustBe functionalError.errorType.toString.toText

              result.rows(1).key.content mustBe "Error pointer".toText
              result.rows(1).value.content mustBe functionalError.pointer.value.toText

              result.rows(2).key.content mustBe "Error reason".toText
              result.rows(2).value.content mustBe reason.toText
          }
        }

        "when original attribute value defined" in {
          forAll(arbitrary[FunctionalError], Gen.alphaNumStr) {
            (functionalError, originalAttributeValue) =>
              val result = functionalError
                .copy(
                  reason = None,
                  originalAttributeValue = Some(originalAttributeValue)
                )
                .toSummaryList

              result.rows.length mustBe 3

              result.rows.head.key.content mustBe "Error type".toText
              result.rows.head.value.content mustBe functionalError.errorType.toString.toText

              result.rows(1).key.content mustBe "Error pointer".toText
              result.rows(1).value.content mustBe functionalError.pointer.value.toText

              result.rows(2).key.content mustBe "Original attribute value".toText
              result.rows(2).value.content mustBe originalAttributeValue.toText
          }
        }

        "when reason and original attribute value defined" in {
          forAll(arbitrary[FunctionalError], Gen.alphaNumStr, Gen.alphaNumStr) {
            (functionalError, reason, originalAttributeValue) =>
              val result = functionalError
                .copy(
                  reason = Some(reason),
                  originalAttributeValue = Some(originalAttributeValue)
                )
                .toSummaryList

              result.rows.length mustBe 4

              result.rows.head.key.content mustBe "Error type".toText
              result.rows.head.value.content mustBe functionalError.errorType.toString.toText

              result.rows(1).key.content mustBe "Error pointer".toText
              result.rows(1).value.content mustBe functionalError.pointer.value.toText

              result.rows(2).key.content mustBe "Error reason".toText
              result.rows(2).value.content mustBe reason.toText

              result.rows(3).key.content mustBe "Original attribute value".toText
              result.rows(3).value.content mustBe originalAttributeValue.toText
          }
        }
      }
    }
  }

  "RichNoReleaseForTransitMessage" - {
    ".toSummaryLists" - {
      "must return summary lists" - {
        "when noReleaseMotivation defined" in {
          forAll(arbitrary[NoReleaseForTransitMessage], Gen.alphaNumStr) {
            (message, noReleaseMotivation) =>
              val result = message
                .copy(noReleaseMotivation = Some(noReleaseMotivation))
                .toSummaryLists

              result.length mustBe 2

              result.head.rows.length mustBe 4

              result.head.rows.head.key.content mustBe "No release motivation".toText
              result.head.rows.head.value.content mustBe noReleaseMotivation.toText

              result.head.rows(1).key.content mustBe "Movement reference number".toText
              result.head.rows(1).value.content mustBe message.mrn.toText

              result.head.rows(2).key.content mustBe "Total number of items".toText
              result.head.rows(2).value.content mustBe message.totalNumberOfItems.toString.toText

              result.head.rows(3).key.content mustBe "Office of departure reference number".toText
              result.head.rows(3).value.content mustBe message.officeOfDepartureRefNumber.toText
          }
        }

        "when noReleaseMotivation undefined" in {
          forAll(arbitrary[NoReleaseForTransitMessage]) {
            message =>
              val result = message
                .copy(noReleaseMotivation = None)
                .toSummaryLists

              result.length mustBe 2

              result.head.rows.length mustBe 3

              result.head.rows.head.key.content mustBe "Movement reference number".toText
              result.head.rows.head.value.content mustBe message.mrn.toText

              result.head.rows(1).key.content mustBe "Total number of items".toText
              result.head.rows(1).value.content mustBe message.totalNumberOfItems.toString.toText

              result.head.rows(2).key.content mustBe "Office of departure reference number".toText
              result.head.rows(2).value.content mustBe message.officeOfDepartureRefNumber.toText
          }
        }

        "when resultsOfControl defined and non-empty" - {
          "when descriptions defined" in {
            forAll(arbitrary[NoReleaseForTransitMessage], arbitrary[Seq[ResultsOfControl]], Gen.alphaNumStr) {
              (message, resultsOfControlList, description) =>
                val result = message
                  .copy(resultsOfControl = Some(resultsOfControlList.zipWithIndex.map {
                    case (resultsOfControl, index) => resultsOfControl.copy(description = Some(s"$description $index"))
                  }))
                  .toSummaryLists

                result.length mustBe 2

                result(1).rows.length mustBe (resultsOfControlList.length * 2)

                for ((resultsOfControl, index) <- resultsOfControlList.zipWithIndex) {
                  result(1).rows(2 * index).key.content mustBe "Control indicator".toText
                  result(1).rows(2 * index).value.content mustBe resultsOfControl.controlIndicator.toText

                  result(1).rows((2 * index) + 1).key.content mustBe "Description".toText
                  result(1).rows((2 * index) + 1).value.content mustBe s"$description $index".toText
                }
            }
          }

          "when descriptions undefined" in {
            forAll(arbitrary[NoReleaseForTransitMessage], arbitrary[Seq[ResultsOfControl]]) {
              (message, resultsOfControlList) =>
                val result = message
                  .copy(resultsOfControl = Some(resultsOfControlList.map(_.copy(description = None))))
                  .toSummaryLists

                result.length mustBe 2

                result(1).rows.length mustBe resultsOfControlList.length

                for ((resultsOfControl, index) <- resultsOfControlList.zipWithIndex) {
                  result(1).rows(index).key.content mustBe "Control indicator".toText
                  result(1).rows(index).value.content mustBe resultsOfControl.controlIndicator.toText
                }
            }
          }
        }

        "when resultsOfControl defined but empty" in {
          forAll(arbitrary[NoReleaseForTransitMessage]) {
            message =>
              val result = message
                .copy(resultsOfControl = Some(Nil))
                .toSummaryLists

              result.length mustBe 2

              result(1).rows.length mustBe 0
          }
        }

        "when resultsOfControl undefined" in {
          forAll(arbitrary[NoReleaseForTransitMessage]) {
            message =>
              val result = message
                .copy(resultsOfControl = None)
                .toSummaryLists

              result.length mustBe 2

              result(1).rows.length mustBe 0
          }
        }
      }
    }
  }
}
