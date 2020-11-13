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
    val statusOptions = Seq(
      Map("title" -> "DepartureSubmitted", "messageKey"           -> "departure.status.submitted", "link"                    -> Nil),
      Map("title" -> "MrnAllocated", "messageKey"                 -> "departure.status.mrnAllocated", "link"                 -> Nil),
      Map("title" -> "TransitDeclarationRejected", "messageKey"   -> "departure.status.transitDeclarationRejected", "link"   -> Nil),
      Map("title" -> "DepartureDeclarationReceived", "messageKey" -> "departure.status.departureDeclarationReceived", "link" -> Nil),
      Map("title" -> "GuaranteeValidationFail", "messageKey"      -> "departure.status.guaranteeValidationFail", "link"      -> Nil),
      Map("title" -> "TransitDeclarationSent", "messageKey"       -> "departure.status.transitDeclarationSent", "link"       -> Nil),
      Map("title" -> "WriteOffNotification", "messageKey"         -> "departure.status.writeOffNotification", "link"         -> Nil)
    )

    "display correct data for each status" - {
      for (status <- statusOptions) {
        s"When status is `${status("title")}` display message and link" in {
          forAll(arbitrary[Departure]) {
            departure =>
              val dep = departure.copy(status = status("title").toString)
              DepartureStatus(dep).status mustBe status("messageKey").toString
              DepartureStatus(dep).actions mustBe status("link")
          }
        }
      }
    }

    "include tad link on ReleasedForTransit status" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val dep = departure.copy(status = "ReleasedForTransit")
          DepartureStatus(dep).status mustBe "departure.status.releasedForTransit"
          DepartureStatus(dep).actions.head.href mustBe s"/manage-transit-movements/departures/${departure.departureId.index}/tad-pdf"
          DepartureStatus(dep).actions.head.key mustBe "departure.downloadTAD"
      }
    }
  }
}
