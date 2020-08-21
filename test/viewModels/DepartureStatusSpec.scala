/*
 * Copyright 2020 HM Revenue & Customs
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

package viewModels

import base.SpecBase
import generators.Generators
import models.Departure
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DepartureStatusSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Departure Status" - {
    val statusus = Seq(
      ("DepartureSubmitted"           -> "departure.status.submitted"),
      ("MrnAllocated"                 -> "departure.status.mrnAllocated"),
      ("ReleasedForTransit"           -> "departure.status.releasedForTransit"),
      ("TransitDeclarationRejected"   -> "departure.status.transitDeclarationRejected"),
      ("DepartureDeclarationReceived" -> "departure.status.departureDeclarationReceived"),
      ("QuaranteeValidationFail"      -> "departure.status.guaranteeValidationFail"),
      ("TransitDeclarationSent"       -> "departure.status.transitDeclarationSent")
    )

    "display correct status" - {
      for (status <- statusus) {
        s"When status is '${status._1}' display correct message '${status._2}'" in {
          forAll(arbitrary[Departure]) {
            departure =>
              val dep = departure.copy(status = status._1)
              DepartureStatus(dep).status mustBe status._2
          }
        }
      }
    }
  }
}
