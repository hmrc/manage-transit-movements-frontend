/*
 * Copyright 2021 HM Revenue & Customs
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

package models.arrival

import base.SpecBase
import models.arrival.ArrivalStatus._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsString, JsSuccess}

class ArrivalStatusSpec extends SpecBase {

  def arrivalStatusesExcluding(exclude: ArrivalStatus*): Seq[ArrivalStatus] =
    ArrivalStatus.values.filterNot(
      x => exclude.toSet.contains(x)
    )

  "must deserialize" - {

    "when given a valid message type" in {

      val gen = Gen.oneOf(ArrivalStatus.values)

      forAll(gen) {
        arrivalStatus =>
          JsString(arrivalStatus.toString).validate[ArrivalStatus] mustEqual JsSuccess(arrivalStatus)
      }
    }

    "when given an invalid message type" in {

      JsString("Something else").validate[ArrivalStatus] mustEqual JsSuccess(InvalidStatus("Invalid status: Something else"))
    }
  }

  "ordering" - {
    "comparing to ArrivalNotificationSubmitted" - {

      "all status must have greater order" in {

        arrivalStatusesExcluding(ArrivalNotificationSubmitted).foreach {
          status =>
            val result = Ordering[ArrivalStatus].max(ArrivalNotificationSubmitted, status)

            result mustBe status
        }
      }
    }

    "comparing to XMLSubmissionNegativeAcknowledgement" - {

      val lesserOrderValues = Seq(
        ArrivalNotificationSubmitted,
        UnloadingRemarksSubmitted
      )

      val greaterOrderValues = Seq(
        XMLSubmissionNegativeAcknowledgement,
        UnloadingPermission,
        UnloadingRemarksRejection,
        GoodsReleased
      )

      "is greater order than ArrivalNotificationSubmitted and UnloadingRemarksRejection" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(XMLSubmissionNegativeAcknowledgement, status)

            result mustBe XMLSubmissionNegativeAcknowledgement
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(XMLSubmissionNegativeAcknowledgement, status)

            result mustBe status
        }
      }
    }

    "comparing to UnloadingRemarksRejection" - {

      val lesserOrderValues = Seq(
        ArrivalNotificationSubmitted,
        ArrivalRejection,
        UnloadingPermission,
        UnloadingRemarksSubmitted,
        XMLSubmissionNegativeAcknowledgement
      )

      val greaterOrderValues = Seq(
        GoodsReleased,
        UnloadingRemarksRejection
      )

      "is greater order than ArrivalNotificationSubmitted, ArrivalRejection, UnloadingPermission and UnloadingRemarksSubmitted" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingRemarksRejection, status)

            result mustBe UnloadingRemarksRejection
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingRemarksRejection, status)

            result mustBe status
        }
      }
    }

    "comparing to UnloadingPermission" - {

      val lesserOrderValues = Seq(
        ArrivalNotificationSubmitted,
        ArrivalRejection,
        XMLSubmissionNegativeAcknowledgement
      )

      val greaterOrderValues = Seq(
        UnloadingPermission,
        UnloadingRemarksSubmitted,
        GoodsReleased,
        UnloadingRemarksRejection
      )

      "is greater order than ArrivalNotificationSubmitted and ArrivalRejection" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingPermission, status)

            result mustBe UnloadingPermission
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingPermission, status)

            result mustBe status
        }
      }
    }

    "comparing to UnloadingRemarksSubmitted" - {

      val lesserOrderValues = Seq(
        ArrivalNotificationSubmitted,
        UnloadingPermission,
        ArrivalRejection,
      )

      val greaterOrderValues = Seq(
        XMLSubmissionNegativeAcknowledgement,
        UnloadingRemarksSubmitted,
        GoodsReleased,
        UnloadingRemarksRejection
      )

      "is greater order than ArrivalNotificationSubmitted, UnloadingPermission, ArrivalRejection" in {

        forAll(Gen.oneOf(lesserOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingRemarksSubmitted, status)

            result mustBe UnloadingRemarksSubmitted
        }
      }

      "in lesser order than any other status" in {

        forAll(Gen.oneOf(greaterOrderValues)) {
          status =>
            val result = Ordering[ArrivalStatus].max(UnloadingRemarksSubmitted, status)

            result mustBe status
        }
      }
    }

    "comparing to GoodsReleased" - {

      "all status must be lesser order" in {

        arrivalStatusesExcluding(GoodsReleased).foreach {
          status =>
            val result = Ordering[ArrivalStatus].max(GoodsReleased, status)

            result mustBe GoodsReleased
        }
      }
    }
  }
}
