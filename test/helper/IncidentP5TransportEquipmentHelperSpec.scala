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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{GoodsReferenceType03, SealType01}
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.IncidentP5TransportEquipmentHelper
import viewModels.sections.Section.AccordionSection

class IncidentP5TransportEquipmentHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "IncidentP5TransportEquipmentHelper" - {

    "rows" - {
      "containerIdentificationNumberRow" - {
        "must return a row" in {
          val transportEquipment = arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(containerIdentificationNumber = Some("12345"))

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.containerIdentificationNumberRow.value

          result.key.value mustEqual "Container identification number"
          result.value.value mustEqual "12345"
          result.actions must not be defined
        }
      }

      "sealIdentificationNumber" - {
        "must return a row" in {

          forAll(nonEmptyString) {
            sealId =>
              val seal = SealType01(1, sealId)
              val transportEquipment =
                arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(
                  Seal = Seq(seal)
                )

              val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
              val result = helper.sealIdentificationNumber(seal).value

              result.key.value mustEqual "Seal 1"
              result.value.value mustEqual sealId
              result.actions must not be defined
          }
        }
      }

      "goodsReferenceNumber" - {
        "must return a row" in {

          forAll(arbitrary[BigInt]) {
            referenceNumber =>
              val goodsReference = GoodsReferenceType03(1, referenceNumber)
              val transportEquipment =
                arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(
                  GoodsReference = Seq(goodsReference)
                )

              val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
              val result = helper.goodsReferenceNumber(goodsReference).value

              result.key.value mustEqual s"Goods item number 1"
              result.value.value mustEqual referenceNumber.toString()
              result.actions must not be defined
          }
        }
      }

    }

    "sections" - {
      "transportEquipmentSection" - {
        "must return an accordion section with correct children" in {
          val transportEquipment =
            arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(
              sequenceNumber = 1,
              containerIdentificationNumber = Some("12345"),
              Seal = Seq(SealType01(1, "id1"), SealType01(2, "id2")),
              GoodsReference = Seq(GoodsReferenceType03(1, 1), GoodsReferenceType03(2, 2))
            )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.transportEquipmentSection

          result mustBe a[AccordionSection]
          result.sectionTitle mustEqual Some("Transport equipment 1")
          result.rows.size mustEqual 1
          result.isOpen mustEqual true
          result.children.size mustEqual 2

          val sealSection = result.children.head
          sealSection mustBe a[AccordionSection]
          sealSection.sectionTitle.value mustEqual "Seals"
          sealSection.rows.size mustEqual 2
          sealSection.isOpen mustEqual false
        }
      }

      "sealSection" - {
        "must return a accordion section with seal rows" in {
          val transportEquipment = arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(
            Seal = Seq(SealType01(1, "id1"), SealType01(2, "id2"))
          )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.sealSection

          result mustBe a[AccordionSection]
          result.sectionTitle.value mustEqual "Seals"
          result.rows.size mustEqual 2
        }
      }

      "goodsReferenceSection" - {
        "must return a accordion section with goodsReference rows" in {
          val transportEquipment = arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(
            GoodsReference = Seq(GoodsReferenceType03(1, 1), GoodsReferenceType03(2, 2))
          )

          val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
          val result = helper.goodsReferenceSection

          result mustBe a[AccordionSection]
          result.sectionTitle.value mustEqual "Goods item numbers"
          result.rows.size mustEqual 2
        }
      }
    }
  }

}
