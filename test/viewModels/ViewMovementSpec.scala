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
import models.referenceData.Movement
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewMovementSpec extends SpecBase with MustMatchers with ModelGenerators with Generators with ScalaCheckPropertyChecks with NunjucksSupport {

  "must serialise to Json" - {
    "when the presentation office is defined" in {
      val viewMovementGen = for {
        vm                     <- arbitrary[ViewMovement]
        presentationOfficeName <- arbitrary[String]
      } yield vm.copy(presentationOfficeName = Some(presentationOfficeName))

      forAll(viewMovementGen) {
        case sut @ ViewMovement(
              date,
              time,
              movementReferenceNumber,
              traderName,
              officeId,
              Some(officeName),
              procedure
            ) =>
          val formatTime =
            time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

          val expectedJson = Json.obj(
            "updated"    -> formatTime,
            "mrn"        -> movementReferenceNumber,
            "traderName" -> traderName,
            "office"     -> s"$officeName ($officeId)",
            "procedure"  -> procedure,
            "actions"    -> Seq("history"),
            "status"     -> "Arrival notification sent"
          )

          Json.toJson(sut) mustBe expectedJson
      }

    }

    "when the presentation office is not defined" in {
      val viewMovementNoName = arbitrary[ViewMovement].map(_.copy(presentationOfficeName = None))

      forAll(viewMovementNoName) {
        case sut @ ViewMovement(
              date,
              time,
              movementReferenceNumber,
              traderName,
              officeId,
              _,
              procedure
            ) =>
          val formatTime =
            time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

          val expectedJson = Json.obj(
            "updated"    -> formatTime,
            "mrn"        -> movementReferenceNumber,
            "traderName" -> traderName,
            "office"     -> officeId,
            "procedure"  -> procedure,
            "actions"    -> Seq("history"),
            "status"     -> "Arrival notification sent"
          )

          Json.toJson(sut) mustBe expectedJson
      }

    }

  }
}
