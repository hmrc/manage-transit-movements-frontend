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

      JsString("Something else").validate[DepartureStatus] mustEqual JsSuccess(InvalidStatus)
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

    "comparing to MrnAllocated" ignore {
      val values = Seq(
        DepartureSubmitted,
        PositiveAcknowledgement,
        DepartureRejected
      )

      "is greater order than DepartureSubmitted, PositiveAcknowledgement, DepartureRejected" in {

        forAll(Gen.oneOf(values)) {
          status =>
            val result = Ordering[DepartureStatus].max(MrnAllocated, status)

            result mustBe MrnAllocated
        }
      }
    }
  }
}
