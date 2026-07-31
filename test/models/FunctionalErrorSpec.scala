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
import generated.{FunctionalErrorType01, FunctionalErrorType07, Number12}
import generators.Generators
import models.FunctionalError.FunctionalErrorWithSection
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class FunctionalErrorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "FunctionalError" - {

    "must deserialise" - {
      "when options are defined" in {
        val json = Json.parse("""
                                |{
                                |  "error": "12",
                                |  "businessRuleId": "BR20004",
                                |  "section": "Trader details",
                                |  "invalidDataItem": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
                                |  "invalidAnswer": "GB635733627000"
                                |}
                                |""".stripMargin)

        val result = json.validate[FunctionalErrorWithSection]

        val expectedResult = FunctionalErrorWithSection(
          error = "12",
          businessRuleId = Some("BR20004"),
          section = Some("Trader details"),
          invalidDataItem = Some(InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber")),
          invalidAnswer = Some("GB635733627000")
        )

        result.get.mustEqual(expectedResult)
      }

      "invalidDataItem undefined" in {
        val json = Json.parse("""
                                |{
                                |  "error": "12",
                                |  "businessRuleId": "BR20004",
                                |  "invalidAnswer": "GB635733627000"
                                |}
                                |""".stripMargin)

        val result = json.validate[FunctionalErrorWithSection]

        val expectedResult = FunctionalErrorWithSection(
          error = "12",
          businessRuleId = Some("BR20004"),
          section = None,
          invalidDataItem = None,
          invalidAnswer = Some("GB635733627000")
        )

        result.get.mustEqual(expectedResult)
      }

      "businessRuleId undefined" in {
        val json = Json.parse("""
                                |{
                                |  "error": "12",
                                |  "invalidDataItem": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
                                |  "invalidAnswer": "GB635733627000"
                                |}
                                |""".stripMargin)

        val result = json.validate[FunctionalErrorWithSection]

        val expectedResult = FunctionalErrorWithSection(
          error = "12",
          businessRuleId = None,
          section = None,
          invalidDataItem = Some(InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber")),
          invalidAnswer = Some("GB635733627000")
        )

        result.get.mustEqual(expectedResult)
      }

      "invalidAnswer undefined" in {
        val json = Json.parse("""
                                |{
                                |  "error": "12",
                                |  "businessRuleId": "BR20004",
                                |  "invalidDataItem": "/CC015C/HolderOfTheTransitProcedure/identificationNumber"
                                |}
                                |""".stripMargin)

        val result = json.validate[FunctionalErrorWithSection]

        val expectedResult = FunctionalErrorWithSection(
          error = "12",
          businessRuleId = Some("BR20004"),
          section = None,
          invalidDataItem = Some(InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber")),
          invalidAnswer = None
        )

        result.get.mustEqual(expectedResult)
      }
    }
  }

  "FunctionalErrorType" - {
    "must serailise" - {

      "when options defined" in {
        val functionalError = FunctionalErrorType(
          FunctionalErrorType07(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
            errorReason = "BR20004",
            originalAttributeValue = Some("GB635733627000")
          )
        )

        val expectedResult = Json.parse("""
                                          |{
                                          |  "errorPointer": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
                                          |  "errorCode": "12",
                                          |  "errorReason": "BR20004",
                                          |  "originalAttributeValue": "GB635733627000"
                                          |}
                                          |""".stripMargin)

        val result = Json.toJson(functionalError)
        result.mustEqual(expectedResult)
      }

      "when errorPointer undefined" in {
        val functionalError = FunctionalErrorType(
          FunctionalErrorType01(
            sequenceNumber = BigInt(1),
            errorPointer = None,
            errorCode = Number12,
            errorReason = Some("BR20005"),
            originalAttributeValue = Some("GB635733627000")
          )
        )

        val expectedResult = Json.parse("""
                                          |{
                                          |  "errorCode": "12",
                                          |  "errorReason": "BR20005",
                                          |  "originalAttributeValue": "GB635733627000"
                                          |}
                                          |""".stripMargin)

        val result = Json.toJson(functionalError)
        result.mustEqual(expectedResult)
      }

      "when errorReason undefined" in {
        val functionalError = FunctionalErrorType(
          FunctionalErrorType01(
            sequenceNumber = BigInt(1),
            errorPointer = Some("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
            errorCode = Number12,
            errorReason = None,
            originalAttributeValue = Some("GB635733627000")
          )
        )

        val expectedResult = Json.parse("""
                                          |{
                                          |  "errorPointer": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
                                          |  "errorCode": "12",
                                          |  "originalAttributeValue": "GB635733627000"
                                          |}
                                          |""".stripMargin)

        val result = Json.toJson(functionalError)
        result.mustEqual(expectedResult)
      }

      "when originalAttributeValue undefined" in {
        val functionalError = FunctionalErrorType(
          FunctionalErrorType07(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
            errorReason = "BR20005",
            originalAttributeValue = None
          )
        )

        val expectedResult = Json.parse("""
                                          |{
                                          |  "errorPointer": "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
                                          |  "errorCode": "12",
                                          |  "errorReason": "BR20005"
                                          |}
                                          |""".stripMargin)

        val result = Json.toJson(functionalError)
        result.mustEqual(expectedResult)
      }
    }
  }
}
