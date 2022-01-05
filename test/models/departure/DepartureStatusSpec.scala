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

package models.departure

import base.SpecBase
import models.departure.DepartureStatus._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsString, JsSuccess}

class DepartureStatusSpec extends SpecBase {

  def departureStatusesExcluding(exclude: DepartureStatus*): Seq[DepartureStatus] =
    DepartureStatus.values.filterNot(
      x => exclude.toSet.contains(x)
    )

  "must deserialize" - {

    "when given a valid message type" in {

      DepartureStatus.values.foreach {
        departureStatus =>
          JsString(departureStatus.toString).validate[DepartureStatus] mustEqual JsSuccess(departureStatus)
      }
    }

    "when given an invalid message type" in {

      JsString("Different status").validate[DepartureStatus] mustEqual JsSuccess(InvalidStatus(s"Invalid status: Different status"))
    }
  }

  "ordering" - {
    "comparing to DepartureSubmitted" - {
      "all status must have greater order" in {

        departureStatusesExcluding(DepartureSubmitted).foreach {
          status =>
            val result = Ordering[DepartureStatus].max(DepartureSubmitted, status)

            result mustBe status
        }
      }
    }

    "comparing to DepartureRejected" - {
      "all status must have greater order except for DepartureSubmitted" in {

        departureStatusesExcluding(DepartureRejected, DepartureSubmitted, PositiveAcknowledgement).foreach {
          status =>
            val result = Ordering[DepartureStatus].max(DepartureRejected, status)

            result mustBe status
        }
      }

      "then it should have great order than PositiveAcknowledgement" in {
        val result = Ordering[DepartureStatus].max(DepartureRejected, PositiveAcknowledgement)

        result mustBe DepartureRejected
      }
    }

    "comparing to PositiveAcknowledgement" - {
      "all status must have greater order except for DepartureSubmitted" in {

        departureStatusesExcluding(PositiveAcknowledgement, DepartureSubmitted).foreach {
          status =>
            val result = Ordering[DepartureStatus].max(PositiveAcknowledgement, status)

            result mustBe status
        }
      }
    }

    "comparing to MrnAllocated" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected
      )

      val greaterOrderValues = Seq(
        ControlDecisionNotification,
        NoReleaseForTransit,
        ReleaseForTransit,
        DeclarationCancellationRequest,
        CancellationDecision,
        WriteOffNotification,
        GuaranteeNotValid,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(MrnAllocated, status)

            result mustBe MrnAllocated
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(MrnAllocated, status)

            result mustBe status
        }
      }
    }

    "comparing to ControlDecision" - {

      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        GuaranteeNotValid
      )

      val greaterOrderValues = Seq(
        ControlDecisionNotification,
        ReleaseForTransit,
        DeclarationCancellationRequest,
        CancellationDecision,
        WriteOffNotification,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected, MrnAllocated, GuaranteeNotValid" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(ControlDecisionNotification, status)

            result mustBe ControlDecisionNotification
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(ControlDecisionNotification, status)

            result mustBe status
        }
      }
    }

    "comparing to GuaranteeNotValid" - {

      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated
      )

      val greaterOrderValues = Seq(
        ControlDecisionNotification,
        ReleaseForTransit,
        DeclarationCancellationRequest,
        CancellationDecision,
        WriteOffNotification,
        GuaranteeNotValid,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected, MrnAllocated" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(GuaranteeNotValid, status)

            result mustBe GuaranteeNotValid
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(GuaranteeNotValid, status)

            result mustBe status
        }
      }
    }

    "comparing to NoReleaseForTransit" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        ControlDecisionNotification,
        GuaranteeNotValid
      )

      val greaterOrderValues = Seq(
        ReleaseForTransit,
        DeclarationCancellationRequest,
        CancellationDecision,
        WriteOffNotification,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected, MrnAllocated, ControlDecision, GuaranteeNotValid" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(NoReleaseForTransit, status)

            result mustBe NoReleaseForTransit
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(NoReleaseForTransit, status)

            result mustBe status
        }
      }
    }

    "comparing to ReleaseForTransit" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        NoReleaseForTransit,
        ControlDecisionNotification,
        GuaranteeNotValid
      )

      val greaterOrderValues = Seq(
        ReleaseForTransit,
        DeclarationCancellationRequest,
        CancellationDecision,
        WriteOffNotification,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected, MrnAllocated, NoReleaseForTransit, Control Decision, GuaranteeNotValid" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(ReleaseForTransit, status)

            result mustBe ReleaseForTransit
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(ReleaseForTransit, status)

            result mustBe status
        }
      }
    }

    "comparing to DeclarationCancellationRequest" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        NoReleaseForTransit,
        ControlDecisionNotification,
        GuaranteeNotValid,
        ReleaseForTransit
      )

      val greaterOrderValues = Seq(
        DeclarationCancellationRequest,
        CancellationDecision,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than " +
        "DepartureSubmitted, " +
        "PositiveAcknowledgement, " +
        "DepartureRejected, " +
        "MrnAllocated, " +
        "NoReleaseForTransit, " +
        "Control Decision, " +
        "GuaranteeNotValid, " +
        "ReleaseForTransit, " +
        "WriteOffNotification" in {

          forAll(Gen.oneOf(lesserOrderValues)) {
            status =>
              val result = Ordering[DepartureStatus].max(DeclarationCancellationRequest, status)

              result mustBe DeclarationCancellationRequest
          }
        }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(DeclarationCancellationRequest, status)

            result mustBe status
        }
      }
    }

    "comparing to CancellationDecision" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        NoReleaseForTransit,
        ControlDecisionNotification,
        GuaranteeNotValid,
        ReleaseForTransit,
        DeclarationCancellationRequest
      )

      val greaterOrderValues = Seq(
        CancellationDecision,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than " +
        "DepartureSubmitted, " +
        "PositiveAcknowledgement, " +
        "DepartureRejected, " +
        "MrnAllocated, " +
        "NoReleaseForTransit, " +
        "Control Decision, " +
        "GuaranteeNotValid, " +
        "ReleaseForTransit, " +
        "WriteOffNotification ," +
        "DeclarationCancellationRequest" in {

          forAll(Gen.oneOf(lesserOrderValues)) {
            status =>
              val result = Ordering[DepartureStatus].max(CancellationDecision, status)

              result mustBe CancellationDecision
          }
        }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(CancellationDecision, status)

            result mustBe status
        }
      }
    }

    "comparing to XMLSubmissionNegativeAcknowledgement" - {
      val lesserOrderValues = Seq(
        DepartureSubmitted,
        DeclarationCancellationRequest
      )

      val greaterOrderValues = Seq(
        PositiveAcknowledgement,
        DepartureRejected,
        MrnAllocated,
        NoReleaseForTransit,
        ControlDecisionNotification,
        GuaranteeNotValid,
        ReleaseForTransit,
        CancellationDecision,
        XMLSubmissionNegativeAcknowledgement
      )

      "is greater order than " +
        "DepartureSubmitted, " +
        "DeclarationCancellationRequest" in {

          forAll(Gen.oneOf(lesserOrderValues)) {
            status =>
              val result = Ordering[DepartureStatus].max(XMLSubmissionNegativeAcknowledgement, status)

              result mustBe XMLSubmissionNegativeAcknowledgement
          }
        }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[DepartureStatus].max(XMLSubmissionNegativeAcknowledgement, status)

            result mustBe status
        }
      }
    }

    "comparing to WriteOffNotification" - {

      "is greater order than all other status" in {

        forAll(Gen.oneOf(DepartureStatus.values)) {
          status =>
            val result = Ordering[DepartureStatus].max(WriteOffNotification, status)

            result mustBe WriteOffNotification
        }
      }
    }

    "comparing to InvalidStatus" - {

      "is lesser order than all other status" in {

        val invalidStatus = InvalidStatus("Other status")

        forAll(Gen.oneOf(DepartureStatus.values)) {
          status =>
            val result = Ordering[DepartureStatus].max(invalidStatus, status)

            result mustBe status
        }
      }
    }
  }
}
