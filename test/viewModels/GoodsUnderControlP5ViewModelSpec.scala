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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.departureP5.{CustomsOfficeOfDeparture, IE060Data, IE060MessageData, RequestedDocument, TransitOperation, TypeOfControls}
import models.referenceData.ControlType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GoodsUnderControlP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  private val controlTypes = Some(Seq(ControlType("42", "Intrusive"), ControlType("44", "Non Intrusive")))

  "headerSection" - {

    "must render rows" in {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
          CustomsOfficeOfDeparture("22323323"),
          None,
          None
        )
      )

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data, controlTypes)

      result.sections.length mustBe 1
      result.sections.head.rows.size mustBe 4
    }

  }
  "control section" - {

    "must not render control information section if controls not present" in {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
          CustomsOfficeOfDeparture("22323323"),
          None,
          None
        )
      )

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data, controlTypes)

      result.sections.length mustBe 1

    }

    "must render control section with  2 rows if controls are present" in {
      val typeOfControls = Some(Seq(TypeOfControls("1", "44", None), TypeOfControls("2", "45", Some("Desc1"))))

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
          CustomsOfficeOfDeparture("22323323"),
          typeOfControls,
          None
        )
      )

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data, controlTypes)

      result.sections.length mustBe 3
      result.sections(1).rows.size mustBe 1
      result.sections(2).rows.size mustBe 2

      result.sections(1).sectionTitle.value mustBe "Control information 1"

    }

  }

  "document section" - {

    "must not render document information section if documents not present" in {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
          CustomsOfficeOfDeparture("22323323"),
          None,
          None
        )
      )

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data, controlTypes)

      result.sections.length mustBe 1

    }

    "must render document section with  2 rows if documents are present" in {

      val requestedDocument = Some(Seq(RequestedDocument("1", "44", None), RequestedDocument("2", "45", Some("Desc1"))))
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
          CustomsOfficeOfDeparture("22323323"),
          None,
          requestedDocument
        )
      )

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider()
      val result            = viewModelProvider.apply(message.data, controlTypes)

      result.sections.length mustBe 3
      result.sections(1).rows.size mustBe 1
      result.sections(2).rows.size mustBe 2

      result.sections(1).sectionTitle.value mustBe "Requested document 1"
      result.sections(2).sectionTitle.value mustBe "Requested document 2"

    }

  }
}
