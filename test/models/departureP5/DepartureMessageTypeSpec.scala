/*
 * Copyright 2023 HM Revenue & Customs
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

package models.departureP5

import base.SpecBase
import generators.Generators
import models.departureP5.DepartureMessageType.UnknownMessageType
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class DepartureMessageTypeSpec extends SpecBase with Generators {

  "must deserialize" - {
    "when given a valid message type" in {
      forAll(Gen.oneOf(DepartureMessageType.values)) {
        departureStatus =>
          JsString(departureStatus.toString).validate[DepartureMessageType] mustEqual JsSuccess(departureStatus)
      }
    }

    "when given an invalid message type" in {
      forAll(nonEmptyString) {
        str =>
          JsString(str).validate[DepartureMessageType] mustEqual JsSuccess(UnknownMessageType(str))
      }
    }
  }

  "must not deserialize" - {
    "when not given a JsString" in {
      val json = Json.obj("foo" -> "bar")
      json.validate[DepartureMessageType] mustBe a[JsError]
    }
  }

}
