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

import java.time.format.DateTimeFormatter

import base.SpecBase
import generators.{Generators, ModelGenerators}
import models.Movement
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewMovementSpec extends SpecBase
  with MustMatchers
  with ModelGenerators
  with Generators
  with ScalaCheckPropertyChecks
  with NunjucksSupport {


  "must serialise to Json" in {

    forAll(arbitrary[Movement], arbitrary[String]) {
      case (Movement(date, time, movementReferenceNumber, traderName, officeId, procedure), officeName) =>

        val sut = ViewMovement(date, time, movementReferenceNumber, traderName, officeId, officeName, procedure)

        val formatTime = sut.time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

        val expectedJson = Json.obj(
          "updated" -> formatTime,
          "mrn" -> sut.movementReferenceNumber,
          "traderName" -> sut.traderName,
          "office" -> s"$officeName ($officeId)",
          "procedure" -> sut.procedure,
          "actions" -> Seq("history"),
          "status" -> "Arrival notification sent"
        )

        Json.toJson(sut) mustBe expectedJson
    }
  }

}
