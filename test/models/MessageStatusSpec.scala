/*
 * Copyright 2024 HM Revenue & Customs
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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class MessageStatusSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "MessageStatus" - {
    "must deserialise" - {
      "when Received" in {
        val json   = JsString("Received")
        val result = json.validate[MessageStatus]
        result.get mustEqual MessageStatus.Received
      }

      "when Pending" in {
        val json   = JsString("Pending")
        val result = json.validate[MessageStatus]
        result.get mustEqual MessageStatus.Pending
      }

      "when Processing" in {
        val json   = JsString("Processing")
        val result = json.validate[MessageStatus]
        result.get mustEqual MessageStatus.Processing
      }

      "when Success" in {
        val json   = JsString("Success")
        val result = json.validate[MessageStatus]
        result.get mustEqual MessageStatus.Success
      }

      "when Failed" in {
        val json   = JsString("Failed")
        val result = json.validate[MessageStatus]
        result.get mustEqual MessageStatus.Failed
      }
    }

    "must fail to deserialise" - {
      "when underlying JSON does not contain a recognised value" in {
        forAll(nonEmptyString) {
          value =>
            val json   = JsString(value)
            val result = json.validate[MessageStatus]
            result.mustBe(a[JsError])
        }
      }

      "when underlying JSON is not a string" in {
        val json   = Json.obj("foo" -> "bar")
        val result = json.validate[MessageStatus]
        result.mustBe(a[JsError])
      }
    }
  }
}
