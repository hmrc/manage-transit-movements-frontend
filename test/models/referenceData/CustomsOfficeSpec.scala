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

package models.referenceData

import base.SpecBase
import cats.data.NonEmptySet
import config.FrontendAppConfig
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json}
import play.api.test.Helpers.running

class CustomsOfficeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "CustomsOffice" - {

    "must serialise" - {
      "when phone number and email address defined" in {
        forAll(nonEmptyString, nonEmptyString, nonEmptyString, nonEmptyString) {
          (id, name, phoneNumber, emailAddress) =>
            val customsOffice = CustomsOffice(id, name, Some(phoneNumber), Some(emailAddress))
            Json.toJson(customsOffice) mustEqual Json.parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name",
                |  "phoneNumber": "$phoneNumber",
                |  "eMailAddress": "$emailAddress"
                |}
                |""".stripMargin)
        }
      }

      "when phone number and email address undefined" in {
        forAll(nonEmptyString, nonEmptyString) {
          (id, name) =>
            val customsOffice = CustomsOffice(id, name, None, None)
            Json.toJson(customsOffice) mustEqual Json.parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name"
                |}
                |""".stripMargin)
        }
      }
    }

    "must deserialise" - {
      "when phase 5" - {
        "when phone number and email address defined" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString, nonEmptyString, nonEmptyString) {
                (id, name, phoneNumber, emailAddress) =>
                  val customsOffice = CustomsOffice(id, name, Some(phoneNumber), Some(emailAddress))
                  Json
                    .parse(s"""
                         |{
                         |  "id": "$id",
                         |  "name": "$name",
                         |  "phoneNumber": "$phoneNumber",
                         |  "eMailAddress": "$emailAddress"
                         |}
                         |""".stripMargin)
                    .as[CustomsOffice](CustomsOffice.reads(config)) mustEqual customsOffice
              }
          }
        }

        "when phone number and email address undefined" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString) {
                (id, name) =>
                  val customsOffice = CustomsOffice(id, name, None, None)
                  Json
                    .parse(s"""
                         |{
                         |  "id": "$id",
                         |  "name": "$name"
                         |}
                         |""".stripMargin)
                    .as[CustomsOffice](CustomsOffice.reads(config)) mustEqual customsOffice
              }
          }
        }
      }

      "when phase 6" - {
        "when phone number and email address defined" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString, nonEmptyString, nonEmptyString) {
                (id, name, phoneNumber, emailAddress) =>
                  val customsOffice = CustomsOffice(id, name, Some(phoneNumber), Some(emailAddress))
                  Json
                    .parse(s"""
                         |{
                         |  "referenceNumber": "$id",
                         |  "customsOfficeLsd": {
                         |    "customsOfficeUsualName": "$name"
                         |  },
                         |  "emailAddress": "$emailAddress",
                         |  "phoneNumber": "$phoneNumber"
                         |}
                         |""".stripMargin)
                    .as[CustomsOffice](CustomsOffice.reads(config)) mustEqual customsOffice
              }
          }
        }

        "when phone number and email address undefined" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString) {
                (id, name) =>
                  val customsOffice = CustomsOffice(id, name, None, None)
                  Json
                    .parse(s"""
                         |{
                         |  "referenceNumber": "$id",
                         |  "customsOfficeLsd": {
                         |    "customsOfficeUsualName": "$name"
                         |  }
                         |}
                         |""".stripMargin)
                    .as[CustomsOffice](CustomsOffice.reads(config)) mustEqual customsOffice
              }
          }
        }
      }
    }

    "must fail to deserialise" - {
      "when json is in unexpected shape" in {
        forAll(nonEmptyString, nonEmptyString) {
          (key, value) =>
            val json = Json.parse(s"""
                 |{
                 |  "$key" : "$value"
                 |}
                 |""".stripMargin)

            val result = json.validate[CustomsOffice]

            result mustBe a[JsError]
        }
      }
    }

    "must order" in {
      val customsOffice1 = CustomsOffice("ID1", "Dhaka", None, None)
      val customsOffice2 = CustomsOffice("ID2", "Copenhagen", None, None)
      val customsOffice3 = CustomsOffice("ID3", "Brussels", None, None)
      val customsOffice4 = CustomsOffice("ID4", "Amsterdam", None, None)

      val customsOffices = NonEmptySet.of(customsOffice1, customsOffice2, customsOffice3, customsOffice4)

      val result = customsOffices.toNonEmptyList.toList

      result mustEqual List(
        customsOffice4,
        customsOffice3,
        customsOffice2,
        customsOffice1
      )
    }

    "listReads" - {
      "when phase 5" - {
        "must read list of customs offices" - {
          "when offices have distinct IDs" in {
            running(_.configure("feature-flags.phase-6-enabled" -> false)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                val json = Json.parse("""
                    |[
                    |  {
                    |    "id" : "AD000001",
                    |    "name" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
                    |    "phoneNumber" : "+ (376) 84 1090",
                    |    "languageCode" : "EN"
                    |  },
                    |  {
                    |    "id" : "AD000002",
                    |    "name" : "DCNJ PORTA",
                    |    "phoneNumber" : "+ (376) 755125",
                    |    "eMailAddress" : "duana.pasdelacasa@andorra.ad",
                    |    "languageCode" : "EN"
                    |  },
                    |  {
                    |    "id": "IT261101",
                    |    "name": "PASSO NUOVO",
                    |    "phoneNumber": "0039 0108619401",
                    |    "eMailAddress": "dogane.genova1.passonuovo@adm.gov.it",
                    |    "languageCode" : "EN"
                    |  }
                    |]
                    |""".stripMargin)

                val result = json.as[List[CustomsOffice]](CustomsOffice.listReads(config))

                result mustEqual List(
                  CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", Some("+ (376) 84 1090"), None),
                  CustomsOffice("AD000002", "DCNJ PORTA", Some("+ (376) 755125"), Some("duana.pasdelacasa@andorra.ad")),
                  CustomsOffice("IT261101", "PASSO NUOVO", Some("0039 0108619401"), Some("dogane.genova1.passonuovo@adm.gov.it"))
                )
            }
          }

          "when offices have duplicate IDs must prioritise the office with an EN language code" in {
            running(_.configure("feature-flags.phase-6-enabled" -> false)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                val json = Json.parse("""
                    |[
                    |  {
                    |    "id" : "AD000001",
                    |    "name" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
                    |    "phoneNumber" : "+ (376) 84 1090",
                    |    "languageCode" : "EN"
                    |  },
                    |  {
                    |    "id" : "AD000001",
                    |    "name" : "ADUANA DE ST. JULIÀ DE LÒRIA",
                    |    "phoneNumber" : "+ (376) 84 1090",
                    |    "languageCode" : "ES"
                    |  },
                    |  {
                    |    "id" : "AD000001",
                    |    "name" : "BUREAU DE SANT JULIÀ DE LÒRIA",
                    |    "phoneNumber" : "+ (376) 84 1090",
                    |    "languageCode" : "FR"
                    |  },
                    |  {
                    |    "id" : "AD000002",
                    |    "name" : "DCNJ PORTA",
                    |    "phoneNumber" : "+ (376) 755125",
                    |    "eMailAddress" : "duana.pasdelacasa@andorra.ad",
                    |    "languageCode" : "EN"
                    |  },
                    |  {
                    |    "id" : "AD000002",
                    |    "name" : "DCNJ PORTA",
                    |    "phoneNumber" : "+ (376) 755125",
                    |    "eMailAddress" : "duana.pasdelacasa@andorra.ad",
                    |    "languageCode" : "ES"
                    |  },
                    |  {
                    |    "id" : "AD000002",
                    |    "name" : "DCNJ PORTA",
                    |    "phoneNumber" : "+ (376) 755125",
                    |    "eMailAddress" : "duana.pasdelacasa@andorra.ad",
                    |    "languageCode" : "FR"
                    |  },
                    |  {
                    |    "id": "IT261101",
                    |    "name": "PASSO NUOVO",
                    |    "phoneNumber": "0039 0108619401",
                    |    "eMailAddress": "dogane.genova1.passonuovo@adm.gov.it",
                    |    "languageCode": "IT"
                    |  }
                    |]
                    |""".stripMargin)

                val result = json.as[List[CustomsOffice]](CustomsOffice.listReads(config))

                result mustEqual List(
                  CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", Some("+ (376) 84 1090"), None),
                  CustomsOffice("AD000002", "DCNJ PORTA", Some("+ (376) 755125"), Some("duana.pasdelacasa@andorra.ad")),
                  CustomsOffice("IT261101", "PASSO NUOVO", Some("0039 0108619401"), Some("dogane.genova1.passonuovo@adm.gov.it"))
                )
            }
          }
        }

        "must fail to read list of customs offices" - {
          "when not an array" in {
            running(_.configure("feature-flags.phase-6-enabled" -> false)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                val json = Json.parse("""
                                        |{
                                        |  "foo" : "bar"
                                        |}
                                        |""".stripMargin)

                val result = json.validate[List[CustomsOffice]](CustomsOffice.listReads(config))

                result mustEqual JsError("Expected customs offices to be in a JsArray")
            }
          }
        }
      }

      "when phase 6" - {
        "must read list of customs offices" - {
          "when offices have distinct IDs" in {
            running(_.configure("feature-flags.phase-6-enabled" -> true)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                val json = Json.parse("""
                    |[
                    |  {
                    |    "referenceNumber" : "AD000001",
                    |    "customsOfficeLsd" : {
                    |      "customsOfficeUsualName" : "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA",
                    |      "languageCode" : "EN"
                    |    },
                    |    "phoneNumber" : "+ (376) 84 1090"
                    |  },
                    |  {
                    |    "referenceNumber" : "AD000002",
                    |    "customsOfficeLsd" : {
                    |      "customsOfficeUsualName" : "DCNJ PORTA",
                    |      "languageCode" : "EN"
                    |    },
                    |    "phoneNumber" : "+ (376) 755125",
                    |    "emailAddress" : "duana.pasdelacasa@andorra.ad"
                    |  },
                    |  {
                    |    "referenceNumber" : "IT261101",
                    |    "customsOfficeLsd" : {
                    |      "customsOfficeUsualName" : "PASSO NUOVO",
                    |      "languageCode" : "IT"
                    |    },
                    |    "phoneNumber": "0039 0108619401",
                    |    "emailAddress": "dogane.genova1.passonuovo@adm.gov.it"
                    |  }
                    |]
                    |""".stripMargin)

                val result = json.as[List[CustomsOffice]](CustomsOffice.listReads(config))

                result mustEqual List(
                  CustomsOffice("AD000001", "CUSTOMS OFFICE SANT JULIÀ DE LÒRIA", Some("+ (376) 84 1090"), None),
                  CustomsOffice("AD000002", "DCNJ PORTA", Some("+ (376) 755125"), Some("duana.pasdelacasa@andorra.ad")),
                  CustomsOffice("IT261101", "PASSO NUOVO", Some("0039 0108619401"), Some("dogane.genova1.passonuovo@adm.gov.it"))
                )
            }
          }
        }

        "must fail to read list of customs offices" - {
          "when not an array" in {
            running(_.configure("feature-flags.phase-6-enabled" -> true)) {
              app =>
                val config = app.injector.instanceOf[FrontendAppConfig]
                val json = Json.parse("""
                                        |{
                                        |  "foo" : "bar"
                                        |}
                                        |""".stripMargin)

                val result = json.validate[List[CustomsOffice]](CustomsOffice.listReads(config))

                result mustEqual JsError("error.expected.jsarray")
            }
          }
        }
      }
    }
  }
}
