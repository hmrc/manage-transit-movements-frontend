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

package models

import base.SpecBase
import generators.ModelGenerators
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.{JsObject, Json}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.viewmodels.NunjucksSupport


class MovementSpec extends SpecBase with MustMatchers with ModelGenerators with ScalaCheckPropertyChecks with NunjucksSupport {

  private def json(movement: Movement): JsObject = Json.obj(
    "movementReferenceNumber" -> movement.movementReferenceNumber,
    "traderName" -> movement.traderName,
    "presentationOffice" -> movement.presentationOffice,
    "procedure" -> movement.procedure
  )

  "Movement" - {

    "Serialise and deserialise" in {

      forAll(arbitrary[Movement]) {
        movement =>
          Json.toJson(movement) mustBe json(movement)
      }
    }

  }
}
