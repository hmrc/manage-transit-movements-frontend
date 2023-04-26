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
import play.api.libs.json.Json

class MetaDataSpec extends SpecBase {

  "must deserialise" - {
    "when lrn undefined" - {
      "and no functional errors" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {}
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = None,
          functionalErrors = Nil
        )
      }

      "and one functional error" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {},
            |            "FunctionalError": [
            |                {
            |                    "errorPointer": "/CC014C",
            |                    "errorCode": "12",
            |                    "errorReason": "N/A"
            |                }
            |            ]
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = None,
          functionalErrors = Seq(
            FunctionalError("/CC014C", "12", "N/A")
          )
        )
      }

      "and multiple functional errors" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {},
            |            "FunctionalError": [
            |                {
            |                    "errorPointer": "/CC014C",
            |                    "errorCode": "12",
            |                    "errorReason": "N/A"
            |                },
            |                {
            |                    "errorPointer": "/CC015C",
            |                    "errorCode": "13",
            |                    "errorReason": "Value too long"
            |                }
            |            ]
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = None,
          functionalErrors = Seq(
            FunctionalError("/CC014C", "12", "N/A"),
            FunctionalError("/CC015C", "13", "Value too long")
          )
        )
      }
    }

    "when lrn defined" - {
      "and no functional errors" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {
            |                "LRN": "TRATESTXI12304231054"
            |            }
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = Some(LocalReferenceNumber("TRATESTXI12304231054")),
          functionalErrors = Nil
        )
      }

      "and one functional error" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {
            |                "LRN": "TRATESTXI12304231054"
            |            },
            |            "FunctionalError": [
            |                {
            |                    "errorPointer": "/CC014C",
            |                    "errorCode": "12",
            |                    "errorReason": "N/A"
            |                }
            |            ]
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = Some(LocalReferenceNumber("TRATESTXI12304231054")),
          functionalErrors = Seq(
            FunctionalError("/CC014C", "12", "N/A")
          )
        )
      }

      "and multiple functional errors" in {
        val json = Json.parse("""
            |{
            |    "body": {
            |        "n1:CC056C": {
            |            "TransitOperation": {
            |                "LRN": "TRATESTXI12304231054"
            |            },
            |            "FunctionalError": [
            |                {
            |                    "errorPointer": "/CC014C",
            |                    "errorCode": "12",
            |                    "errorReason": "N/A"
            |                },
            |                {
            |                    "errorPointer": "/CC015C",
            |                    "errorCode": "13",
            |                    "errorReason": "Value too long"
            |                }
            |            ]
            |        }
            |    }
            |}
            |""".stripMargin)

        json.as[MetaData] mustBe MetaData(
          lrn = Some(LocalReferenceNumber("TRATESTXI12304231054")),
          functionalErrors = Seq(
            FunctionalError("/CC014C", "12", "N/A"),
            FunctionalError("/CC015C", "13", "Value too long")
          )
        )
      }
    }
  }

}
