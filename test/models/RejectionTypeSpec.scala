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

package models

import base.SpecBase
import generators.Generators
import models.RejectionType.Other
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, Json}

class RejectionTypeSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "RejectionType" - {

    "must deserialise when recognised type" in {
      forAll(arbitrary[RejectionType]) {
        rejectionType =>
          JsString(rejectionType.code).as[RejectionType] mustEqual rejectionType
      }
    }

    "must deserialise when other " in {
      JsString("differentCode").as[RejectionType] mustEqual Other("differentCode")
    }

    "must serialise" in {
      forAll(arbitrary[RejectionType]) {
        rejectionType =>
          Json.toJson(rejectionType) mustEqual JsString(rejectionType.code)
      }
    }

  }
}
