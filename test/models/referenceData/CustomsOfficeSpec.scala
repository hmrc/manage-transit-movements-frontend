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
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsSuccess, Json}

class CustomsOfficeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "CustomsOffice" - {

    "serialize to JSON" in {
      val customsOffice = CustomsOffice("ID123", "Customs Office Name", Some("123456789"))
      val expectedJson = Json.obj(
        "id"          -> "ID123",
        "name"        -> "Customs Office Name",
        "phoneNumber" -> "123456789"
      )

      val json = Json.toJson(customsOffice)
      json mustBe expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "id"          -> "ID123",
        "name"        -> "Customs Office Name",
        "phoneNumber" -> "123456789"
      )

      val expectedCustomsOffice = CustomsOffice("ID123", "Customs Office Name", Some("123456789"))

      json.validate[CustomsOffice] mustBe JsSuccess(expectedCustomsOffice)
    }

    "nameAndCode must return correct string" in {
      CustomsOffice("GB00006", "BOSTON", None).nameAndCode `mustBe` "BOSTON (GB00006)"
    }

    "must deserialise" - {
      "when there is a customs office" - {
        "with all data" in {
          val json = Json.parse("""
              |    {
              |        "id": "GB00006",
              |        "name": "BOSTON",
              |        "phoneNumber": "01234567890"
              |    }
              |""".stripMargin)

          json.as[CustomsOffice] `mustBe` CustomsOffice("GB00006", "BOSTON", Some("01234567890"))
        }
        "without a phone number" in {
          val json = Json.parse("""
              |    {
              |        "id": "GB00006",
              |        "name": "BOSTON"
              |    }
              |""".stripMargin)

          json.as[CustomsOffice] `mustBe` CustomsOffice("GB00006", "BOSTON", None)
        }
      }
    }

    "listReads" - {
      "must read list of customs offices" - {
        "when offices have distinct IDs" in {
          val json = Json.parse("""
                                  |[
                                  |  {
                                  |    "id" : "AD000001",
                                  |    "name" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "EN"
                                  |  },
                                  |  {
                                  |    "id" : "AD000002",
                                  |    "name" : "DCNJ PORTA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "EN"
                                  |  },
                                  |  {
                                  |    "id": "IT261101",
                                  |    "name": "PASSO NUOVO",
                                  |    "countryId": "IT",
                                  |    "languageCode": "IT"
                                  |  }
                                  |]
                                  |""".stripMargin)

          val result = json.as[List[CustomsOffice]]

          result `mustBe` List(
            CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", None),
            CustomsOffice("AD000002", "DCNJ PORTA", None),
            CustomsOffice("IT261101", "PASSO NUOVO", None)
          )
        }

        "when offices have duplicate IDs must prioritise the office with an EN language code" in {
          val json = Json.parse("""
                                  |[
                                  |  {
                                  |    "id" : "AD000001",
                                  |    "name" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "EN"
                                  |  },
                                  |  {
                                  |    "id" : "AD000001",
                                  |    "name" : "ADUANA DE ST. JULIÀ DE LÒRIA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "ES"
                                  |  },
                                  |  {
                                  |    "id" : "AD000001",
                                  |    "name" : "BUREAU DE SANT JULIÀ DE LÒRIA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "FR"
                                  |  },
                                  |  {
                                  |    "id" : "AD000002",
                                  |    "name" : "DCNJ PORTA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "FR"
                                  |  },
                                  |  {
                                  |    "id" : "AD000002",
                                  |    "name" : "DCNJ PORTA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "ES"
                                  |  },
                                  |  {
                                  |    "id" : "AD000002",
                                  |    "name" : "DCNJ PORTA",
                                  |    "countryId" : "AD",
                                  |    "languageCode" : "EN"
                                  |  },
                                  |  {
                                  |    "id": "IT261101",
                                  |    "name": "PASSO NUOVO",
                                  |    "countryId": "IT",
                                  |    "languageCode": "IT"
                                  |  }
                                  |]
                                  |""".stripMargin)

          val result = json.as[List[CustomsOffice]]

          result `mustBe` List(
            CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", None),
            CustomsOffice("AD000002", "DCNJ PORTA", None),
            CustomsOffice("IT261101", "PASSO NUOVO", None)
          )
        }
      }

      "must fail to read list of customs offices" - {
        "when not an array" in {
          val json = Json.parse("""
                                  |{
                                  |  "foo" : "bar"
                                  |}
                                  |""".stripMargin)

          val result = json.validate[List[CustomsOffice]]

          result `mustBe` JsError("Expected customs offices to be in a JsArray")
        }
      }
    }
  }

}
