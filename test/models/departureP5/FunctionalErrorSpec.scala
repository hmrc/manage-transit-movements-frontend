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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class FunctionalErrorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "FunctionalError" - {

    "must deserialise" - {
      "when there are no functional errors" in {
        val json = Json.parse("""
            |[]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Nil
      }

      "when there is one functional error" in {
        val json = Json.parse("""
            |[
            |    {
            |        "errorPointer": "/CC014C",
            |        "errorCode": "12",
            |        "errorReason": "N/A"
            |    }
            |]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Seq(
          FunctionalError("/CC014C", "12", "N/A", None)
        )
      }

      "when there are multiple functional errors" in {
        val json = Json.parse("""
            |[
            |    {
            |        "errorPointer": "/CC014C",
            |        "errorCode": "12",
            |        "errorReason": "N/A"
            |    },
            |    {
            |        "errorPointer": "/CC015C/Authorisation[1]/referenceNumber",
            |        "errorCode": "14",
            |        "errorReason": "G0033",
            |        "originalAttributeValue": "XIDEP01"
            |    }
            |]
            |""".stripMargin)

        json.as[Seq[FunctionalError]] mustBe Seq(
          FunctionalError("/CC014C", "12", "N/A", None),
          FunctionalError("/CC015C/Authorisation[1]/referenceNumber", "14", "G0033", Some("XIDEP01"))
        )
      }
    }

    "isAmendable" - {
      "must return true" - {
        "when error pointer starts with /CC015C/" in {
          val errorPointer = "/CC015C/Authorisation[1]/referenceNumber"
          forAll(arbitrary[FunctionalError].map(_.copy(errorPointer = errorPointer))) {
            functionalError =>
              functionalError.isAmendable mustBe true
          }
        }
      }

      "must return false" - {
        "when error pointer doesn't start with /CC015C/" in {
          val errorPointer = "/CC014C"
          forAll(arbitrary[FunctionalError].map(_.copy(errorPointer = errorPointer))) {
            functionalError =>
              functionalError.isAmendable mustBe false
          }
        }
      }
    }
  }

}
