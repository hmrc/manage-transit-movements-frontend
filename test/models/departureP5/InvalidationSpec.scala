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
import play.api.libs.json.{JsError, Json}

import java.time.LocalDateTime

class InvalidationSpec extends SpecBase {

  private val decisionDateAndTime: LocalDateTime = LocalDateTime.now()
  private val justification                      = "This is my justification for this decision"

  "must deserialise" - {

    "when boolean values are 0" in {
      val json = Json.parse(s"""
          |{
          |  "decisionDateAndTime" : "$decisionDateAndTime",
          |  "decision" : "0",
          |  "initiatedByCustoms" : "0",
          |  "justification" : "$justification"
          |}
          |""".stripMargin)

      val result = json.as[Invalidation]

      result mustBe Invalidation(
        decisionDateAndTime = Some(decisionDateAndTime),
        decision = false,
        initiatedByCustoms = false,
        justification = Some(justification)
      )
    }

    "when boolean values are 1" in {
      val json = Json.parse(s"""
          |{
          |  "decisionDateAndTime" : "$decisionDateAndTime",
          |  "decision" : "1",
          |  "initiatedByCustoms" : "1",
          |  "justification" : "$justification"
          |}
          |""".stripMargin)

      val result = json.as[Invalidation]

      result mustBe Invalidation(
        decisionDateAndTime = Some(decisionDateAndTime),
        decision = true,
        initiatedByCustoms = true,
        justification = Some(justification)
      )
    }
  }

  "must fail to deserialise" - {
    "when decision / initiatedByCustoms are neither 0 or 1" in {
      val json = Json.parse(s"""
           |{
           |  "decisionDateAndTime" : "$decisionDateAndTime",
           |  "decision" : "foo",
           |  "initiatedByCustoms" : "bar",
           |  "justification" : "$justification"
           |}
           |""".stripMargin)

      val result = json.validate[Invalidation]

      result mustBe a[JsError]
    }
  }

}
