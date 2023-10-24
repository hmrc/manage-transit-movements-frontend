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

package models.arrivalP5

import base.SpecBase
import generators.Generators
import models.ArrivalRejectionType
import models.departureP5.FunctionalError
import org.scalacheck.Arbitrary
import play.api.libs.json._

class IE057DataSpec extends SpecBase with Generators {

  "IE057Data" - {

    "must deserialize" in {
      val arrivalRejectionType: ArrivalRejectionType = Arbitrary.arbitrary[ArrivalRejectionType].sample.value

      val json = Json.parse(s"""
          |{
          | "body": {
          |   "n1:CC057C": {
          |     "TransitOperation": {
          |       "MRN": "AB123",
          |       "businessRejectionType": "${arrivalRejectionType.code}"
          |     },
          |     "CustomsOfficeOfDestinationActual": {
          |       "referenceNumber": "1234"
          |     },
          |     "FunctionalError": [
          |     {
          |       "errorPointer": "1",
          |       "errorCode": "12",
          |       "errorReason": "Codelist violation"
          |     },
          |     {
          |       "errorPointer": "2",
          |       "errorCode": "14",
          |       "errorReason": "Rule violation",
          |       "originalAttributeValue": "test"
          |     }
          |    ]
          |   }
          | }
          |}
          |""".stripMargin)

      val expectedResult = IE057Data(
        IE057MessageData(
          TransitOperationIE057(
            "AB123",
            arrivalRejectionType
          ),
          CustomsOfficeOfDestinationActual(
            "1234"
          ),
          Seq(
            FunctionalError("1", "12", "Codelist violation", None),
            FunctionalError("2", "14", "Rule violation", Some("test"))
          )
        )
      )

      val result: IE057Data = json.validate[IE057Data].asOpt.value

      result mustBe expectedResult
    }
  }
}
