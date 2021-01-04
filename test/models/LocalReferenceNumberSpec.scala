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

package models

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsString, Json}

class LocalReferenceNumberSpec extends AnyFreeSpec with Generators with Matchers with EitherValues {

  "a Local Reference Number" - {

    "must deserialise" in {
      forAll(arbitrary[LocalReferenceNumber]) {
        lrn =>
          JsString(lrn.toString).as[LocalReferenceNumber] mustEqual lrn
      }
    }

    "must serialise" in {
      forAll(arbitrary[LocalReferenceNumber]) {
        lrn =>
          Json.toJson(lrn) mustEqual JsString(lrn.toString)
      }
    }

    "must treat .apply and .toString as dual" in {

      forAll(arbitrary[LocalReferenceNumber]) {
        lrn =>
          new LocalReferenceNumber(lrn.toString).value mustEqual lrn.toString
      }
    }
  }
}
