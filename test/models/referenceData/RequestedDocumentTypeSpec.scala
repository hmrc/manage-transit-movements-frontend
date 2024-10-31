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

package models.referenceData

import base.SpecBase
import cats.data.NonEmptySet
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class RequestedDocumentTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RequestedDocumentType" - {

    "toString must return correct string when" - {
      "description is empty" in {
        RequestedDocumentType("C620", "").toString `mustBe` "C620"
      }
      "description is not empty" in {
        RequestedDocumentType("C620", "T2LF document").toString `mustBe` "C620 - T2LF document"
      }
    }

    "must deserialise" - {
      "when there is a requested document type" in {
        val json = Json.parse("""
              |    {
              |        "code": "C620",
              |        "description": "T2LF document"
              |    }
              |""".stripMargin)

        json.as[RequestedDocumentType] `mustBe` RequestedDocumentType("C620", "T2LF document")
      }
    }

    "serialize to JSON correctly" in {
      val requestedDocumentType = RequestedDocumentType(
        code = "DOC001",
        description = "Support doc"
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "code": "DOC001",
          |  "description": "Support doc"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(requestedDocumentType)
      json mustEqual expectedJson
    }

    "order RequestedDocumentType instances by code" in {
      val unorderedDocs = Seq(
        RequestedDocumentType("DOC003", "Previous"),
        RequestedDocumentType("DOC001", "Support"),
        RequestedDocumentType("DOC002", "Support")
      )

      val orderedDocs = Seq(
        RequestedDocumentType("DOC001", "Support"),
        RequestedDocumentType("DOC002", "Support"),
        RequestedDocumentType("DOC003", "Previous")
      )

      val result = NonEmptySet
        .of(unorderedDocs.head, unorderedDocs.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedDocs)
    }
  }

}
