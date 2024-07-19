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

package helper

import base.SpecBase
import generated.{GoodsReferenceType01, SealType04}
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.IncidentP5TransportEquipmentHelper
import viewModels.sections.Section.AccordionSection

class IncidentP5TransportEquipmentHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "IncidentP5TransportEquipmentHelper" - {

    "rows" - {
      "containerIdentificationNumberRow" - {
        "must return a row" in {
          val transportEquipment = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(containerIdentificationNumber = Some("12345"))

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.containerIdentificationNumberRow.value

          result.key.value mustBe "Container identification number"
          result.value.value mustBe "12345"
          result.actions must not be defined
        }
      }

      "sealIdentificationNumber" - {
        "must return a row" in {

          forAll(nonEmptyString) {
            sealId =>
              val seal = SealType04("1", sealId)
              val transportEquipment =
                arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(
                  Seal = Seq(seal)
                )

              val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
              val result = helper.sealIdentificationNumber(seal).value

              result.key.value mustBe "Seal 1"
              result.value.value mustBe sealId
              result.actions must not be defined
          }
        }
      }

      "goodsReferenceNumber" - {
        "must return a row" in {

          forAll(arbitrary[BigInt]) {
            referenceNumber =>
              val goodsReference = GoodsReferenceType01("1", referenceNumber)
              val transportEquipment =
                arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(
                  GoodsReference = Seq(goodsReference)
                )

              val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
              val result = helper.goodsReferenceNumber(goodsReference).value

              result.key.value mustBe s"Goods item number 1"
              result.value.value mustBe referenceNumber.toString()
              result.actions must not be defined
          }
        }
      }

    }

    "sections" - {
      "transportEquipmentSection" - {
        "must return an accordion section with correct children" in {
          val transportEquipment =
            arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(
              sequenceNumber = "1",
              containerIdentificationNumber = Some("12345"),
              Seal = Seq(SealType04("1", "id1"), SealType04("2", "id2")),
              GoodsReference = Seq(GoodsReferenceType01("1", 1), GoodsReferenceType01("2", 2))
            )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.transportEquipmentSection

          result mustBe a[AccordionSection]
          result.sectionTitle mustBe Some("Transport equipment 1")
          result.rows.size mustBe 1
          result.isOpen mustBe true
          result.children.size mustBe 2

          val sealSection = result.children.head
          sealSection mustBe a[AccordionSection]
          sealSection.sectionTitle.value mustBe "Seals"
          sealSection.rows.size mustBe 2
          sealSection.isOpen mustBe false
        }
      }

      "sealSection" - {
        "must return a accordion section with seal rows" in {
          val transportEquipment = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(
            Seal = Seq(SealType04("1", "id1"), SealType04("2", "id2"))
          )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.sealSection

          result mustBe a[AccordionSection]
          result.sectionTitle.value mustBe "Seals"
          result.rows.size mustBe 2
        }
      }

      "goodsReferenceSection" - {
        "must return a accordion section with goodsReference rows" in {
          val transportEquipment = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(
            GoodsReference = Seq(GoodsReferenceType01("1", 1), GoodsReferenceType01("2", 2))
          )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.goodsReferenceSection

          result mustBe a[AccordionSection]
          result.sectionTitle.value mustBe "Goods item numbers"
          result.rows.size mustBe 2
        }
      }
    }
  }
}
