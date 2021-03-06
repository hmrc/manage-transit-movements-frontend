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

package viewModels

import java.time.format.DateTimeFormatter

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class ViewDepartureSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must serialise to Json" in {
    forAll(arbitrary[ViewDeparture]) {
      viewDeparture =>
        val expectedJson = Json.obj(
          "updatedDate" -> viewDeparture.updatedDate,
          "updatedTime" -> viewDeparture.updatedTime
            .format(DateTimeFormatter.ofPattern("h:mma"))
            .toLowerCase,
          "localReferenceNumber" -> viewDeparture.localReferenceNumber,
          "status"               -> viewDeparture.status,
          "actions"              -> viewDeparture.actions
        )

        Json.toJson(viewDeparture) mustBe expectedJson
    }
  }
}
