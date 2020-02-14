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

import generators.ModelGenerators
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class MovementSpec extends FreeSpec with MustMatchers with ModelGenerators with ScalaCheckPropertyChecks {

  val model = Movement("updated", "mrn", "traderName", "office", "procedure", "status", "action")

  val json = Json.obj(
    "update" -> "updated",
    "mrn"->"mrn",
    "traderName"->"traderName",
    "office"->"office",
    "procedure"->"procedure",
    "status"->"status",
    "action"->"action")

  "Movement" - {
    "Serialise to Json" in {
      Json.toJson(model) mustBe json
    }

    "Deserialise to Model" in {
      json.as[Movement] mustBe model
    }

    "Serialise and deserialise" in {

      forAll(arbitrary[Movement]) {
        movement =>
          val json = Json.toJson(movement)
          json.as[Movement] mustBe movement
      }
    }
  }
}
