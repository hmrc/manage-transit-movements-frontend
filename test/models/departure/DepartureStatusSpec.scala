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
import generators.Generators
import models.departure.DepartureStatus._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, JsSuccess}
import org.scalacheck.Arbitrary.arbitrary

class DepartureStatusSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  def departureStatusesExcluding(exclude: DepartureStatus*): Seq[DepartureStatus] =
    DepartureStatus.values.filterNot(
      x => exclude.toSet.contains(x)
    )

  "must deserialize" - {

    "when given a valid message type" in {

      forAll(arbitrary[DepartureStatus]) {
        departureStatus =>
          JsString(departureStatus.toString).validate[DepartureStatus] mustEqual JsSuccess(departureStatus)
      }
    }

    "when given an invalid message type" in {

      JsString("Different status").validate[DepartureStatus] mustEqual JsSuccess(InvalidStatus(s"Invalid status: Different status"))
    }
  }
}
