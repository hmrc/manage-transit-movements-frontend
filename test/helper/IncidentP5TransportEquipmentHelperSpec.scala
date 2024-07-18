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
import generators.Generators
import models.Index
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.IncidentP5TransportEquipmentHelper
import viewModels.sections.Section.{AccordionSection, StaticSection}

class IncidentP5TransportEquipmentHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "IncidentP5TransportEquipmentHelper" - {

    "rows" - {
      "containerIdentificationNumberRow" - {
        "must return a row" in {
          val transportEquipment = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(containerIdentificationNumber = Some("12345"))

          val helper = new IncidentP5TransportEquipmentHelper(Seq(transportEquipment))
          val result = helper.containerIdentificationNumberRow(Index(0)).value

          result.key.value mustBe "Container identification number"
          result.value.value mustBe "12345"
          result.actions must not be defined
        }
      }

    }

    "sections" - {
      "transportEquipmentSection" - {
        "must return an accordion section" in {
          val transportEquipment = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(containerIdentificationNumber = Some("12345"))
          val helper             = new IncidentP5TransportEquipmentHelper(Seq(transportEquipment))
          val result             = helper.transportEquipmentSection(Index(0))

          result mustBe a[AccordionSection]
          result.sectionTitle mustBe Some("Transport equipment 1")
          result.rows.size mustBe 1
          result.children.size mustBe 0
          result.isOpen mustBe true
        }
      }

      "transportEquipmentsSection" - {
        "must return a static section" in {
          val transportEquipment1 = arbitraryTransportEquipmentType07.arbitrary.sample.value
          val transportEquipment2 = arbitraryTransportEquipmentType07.arbitrary.sample.value
          val transportEquipments = Seq(transportEquipment1, transportEquipment2)
          val helper              = new IncidentP5TransportEquipmentHelper(transportEquipments)
          val result              = helper.transportEquipmentsSection

          result mustBe a[StaticSection]
          result.rows.size mustBe 0
          result.children.size mustBe 2

          result.children.head mustBe a[AccordionSection]
          result.children.head.sectionTitle mustBe Some("Transport equipment 1")
          result.children.head.isOpen mustBe true

          result.children(1) mustBe a[AccordionSection]
          result.children(1).sectionTitle mustBe Some("Transport equipment 2")
          result.children(1).isOpen mustBe false
        }
      }
    }
  }
}
