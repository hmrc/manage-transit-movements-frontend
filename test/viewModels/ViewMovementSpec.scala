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
import generators.ModelGenerators
import models.Arrival
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewMovementSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks with NunjucksSupport {

  "must serialise to Json" in {

    forAll(arbitrary[ViewMovement]) {
      viewMovement =>
        val formatTime =
          viewMovement.time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

        val expectedJson = Json.obj(
          "updated" -> formatTime,
          "mrn"     -> viewMovement.movementReferenceNumber,
          "status"  -> viewMovement.status,
          "actions" -> viewMovement.action
        )

        Json.toJson(viewMovement) mustBe expectedJson
    }
  }

  "must display unloading permission status" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        arrival.copy(status = "UnloadingPermission")

        val viewMovement = ViewMovement(arrival)(messages,frontendAppConfig)

      val expectedResult = ViewMovement(arrival.created, )




    }
    }


}
