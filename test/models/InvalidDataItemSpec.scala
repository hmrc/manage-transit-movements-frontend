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
import play.api.libs.json.{JsError, JsString, Json}

class InvalidDataItemSpec extends SpecBase {

  "InvalidDataItem" - {

    "reads" - {
      "must deserialise" - {
        "when json is a JsString" in {
          val json           = JsString("/CC015C/HolderOfTheTransitProcedure/identificationNumber")
          val result         = json.validate[InvalidDataItem]
          val expectedResult = "Holder of the transit procedure: Identification number"
          result.get.value.mustBe(expectedResult)
        }
      }

      "must fail to deserialise" - {
        "when json is not a JsString" in {
          val json   = Json.obj("foo" -> "bar")
          val result = json.validate[InvalidDataItem]
          result.mustBe(a[JsError])
        }
      }
    }

    "apply" - {
      "when /CC015C/HolderOfTheTransitProcedure/identificationNumber" - {
        "must return Holder of the transit procedure: Identification number" in {
          val errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("Holder of the transit procedure: Identification number")
        }
      }

      "when /CC015C/Consignment/HouseConsignment[50]/ConsignmentItem[10]/consignor" - {
        "must return House consignment 50: Consignment item 10: Consignor" in {
          val errorPointer = "/CC015C/Consignment/HouseConsignment[50]/ConsignmentItem[10]/consignor"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("House consignment 50: Consignment item 10: Consignor")
        }
      }

      "when /CC015C/Consignment/HouseConsignment[5]/ConsignmentItem[10]/SupportingDocument[22]/type" - {
        "must return House consignment 5: Consignment item 10: Supporting document 22: Type" in {
          val errorPointer = "/CC015C/Consignment/HouseConsignment[5]/ConsignmentItem[10]/SupportingDocument[22]/type"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("House consignment 5: Consignment item 10: Supporting document 22: Type")
        }
      }

      "when //Consignment/LocationOfGoods" - {
        "must return Location of goods" in {
          val errorPointer = "//Consignment/LocationOfGoods"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("Location of goods")
        }
      }

      "when /CC015C/Consignment/referenceNumberUCR" - {
        "must return Reference number UCR" in {
          val errorPointer = "/CC015C/Consignment/referenceNumberUCR"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("Reference number UCR")
        }
      }

      "when /CC007C/Authorisation[1]/referenceNumber" - {
        "must return Authorisation 1: Reference number" in {
          val errorPointer = "/CC007C/Authorisation[1]/referenceNumber"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("Authorisation 1: Reference number")
        }
      }

      "when /CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber" - {
        "must return Holder of the transit procedure: TIR holder identification number" in {
          val errorPointer = "/CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber"
          val result       = InvalidDataItem(errorPointer)
          result.value.mustBe("Holder of the transit procedure: TIR holder identification number")
        }
      }
    }
  }
}
