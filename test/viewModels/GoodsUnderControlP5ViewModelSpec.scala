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
import models.departureP5.IE060MessageType.{GoodsUnderControl, GoodsUnderControlRequestedDocuments}
import models.departureP5._
import models.referenceData.{ControlType, CustomsOffice}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsUnderControlP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  private val customsOffice = CustomsOffice("22323323", "", None)

  "GoodsUnderControlP5ViewModel" - {

    val typeOfControls    = Some(Seq(TypeOfControls("1", "44", None)))
    val controlType44     = ControlType("44", "")
    val requestedDocument = Some(Seq(RequestedDocument("1", "44", None)))

    "when no requested documents and type 0" - {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(
            Some("MRN1"),
            Some("LRN1"),
            LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
            GoodsUnderControl
          ),
          CustomsOfficeOfDeparture("22323323"),
          typeOfControls,
          None
        )
      )

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must render correct number of sections" in {
        result.sections.length mustBe 2
      }

      "must return correct title" in {
        result.title mustBe "Goods under control"
      }
      "must return correct heading" in {
        result.heading mustBe "Goods under control"
      }
      "must return correct paragraphs" in {
        result.paragraph1 mustBe "Customs have placed this declaration under control while they carry out further checks. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 mustBe "While under control, the goods will remain under supervision at the office of destination."
        result.paragraph3 mustBe "Once Customs have completed their checks, they will notify you with the outcome."
      }
      "must return correct end paragraph" in {
        result.type0LinkPrefix mustBe "You must wait for the outcome of Customs’ checks."
        result.type0LinkText mustBe "Check your departure declarations"
        result.type0LinkTextSuffix mustBe "for further updates."
      }
    }

    "when no requested documents and type 1" - {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(
            Some("MRN1"),
            Some("LRN1"),
            LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
            GoodsUnderControlRequestedDocuments
          ),
          CustomsOfficeOfDeparture("22323323"),
          typeOfControls,
          None
        )
      )

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must render correct number of sections" in {
        result.sections.length mustBe 1
      }

      "must return correct title" in {
        result.title mustBe "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading mustBe "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 mustBe "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 mustBe "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 mustBe "You must contact the office of destination directly to share the requested documentation."
      }
    }

    "when there is requested documents and type 0" - {
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(
            Some("MRN1"),
            Some("LRN1"),
            LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
            GoodsUnderControl
          ),
          CustomsOfficeOfDeparture("22323323"),
          typeOfControls,
          requestedDocument
        )
      )

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must render correct number of sections" in {
        result.sections.length mustBe 3
      }

      "must return correct title" in {
        result.title mustBe "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading mustBe "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 mustBe "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 mustBe "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 mustBe "You must contact the office of destination directly to share the requested documentation."
      }
    }

    "when there is requested documents and type 1" - {
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(
            Some("MRN1"),
            Some("LRN1"),
            LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
            GoodsUnderControlRequestedDocuments
          ),
          CustomsOfficeOfDeparture("22323323"),
          None,
          requestedDocument
        )
      )

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must render correct number of sections" in {
        result.sections.length mustBe 2
      }

      "must return correct title" in {
        result.title mustBe "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading mustBe "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 mustBe "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 mustBe "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 mustBe "You must contact the office of destination directly to share the requested documentation."
      }
    }

    "must render rows" in {

      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(
            Some("MRN1"),
            Some("LRN1"),
            LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
            GoodsUnderControlRequestedDocuments
          ),
          CustomsOfficeOfDeparture("22323323"),
          None,
          None
        )
      )

      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      result.sections.length mustBe 1
      result.sections.head.rows.size mustBe 4
    }

    "control section" - {

      "must not render control information section if controls not present" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(
              Some("MRN1"),
              Some("LRN1"),
              LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
              GoodsUnderControlRequestedDocuments
            ),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

        val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(message.data).futureValue

        result.sections.length mustBe 1

      }

      "must render control section with 2 rows if controls are present" in {
        val typeOfControls = Some(Seq(TypeOfControls("1", "44", None), TypeOfControls("2", "45", Some("Desc1"))))
        val controlType44  = ControlType("44", "Nature and characteristics of the goods")
        val controlType45  = ControlType("45", "")

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(
              Some("MRN1"),
              Some("LRN1"),
              LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
              GoodsUnderControl
            ),
            CustomsOfficeOfDeparture("22323323"),
            typeOfControls,
            None
          )
        )

        when(mockReferenceDataService.getControlType("44")).thenReturn(Future.successful(controlType44))
        when(mockReferenceDataService.getControlType("45")).thenReturn(Future.successful(controlType45))
        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

        val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(message.data).futureValue

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
            TransitOperationIE060(
              Some("MRN1"),
              Some("LRN1"),
              LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
              GoodsUnderControlRequestedDocuments
            ),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

        val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(message.data).futureValue

        result.sections.length mustBe 1

      }

      "must render document section with 2 rows if documents are present" in {

        val requestedDocument = Some(Seq(RequestedDocument("1", "44", None), RequestedDocument("2", "45", Some("Desc1"))))
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(
              Some("MRN1"),
              Some("LRN1"),
              LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
              GoodsUnderControlRequestedDocuments
            ),
            CustomsOfficeOfDeparture("22323323"),
            None,
            requestedDocument
          )
        )

        when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

        val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(message.data).futureValue

        result.sections.length mustBe 3
        result.sections(1).rows.size mustBe 1
        result.sections(2).rows.size mustBe 2

        result.sections(1).sectionTitle.value mustBe "Requested document 1"
        result.sections(2).sectionTitle.value mustBe "Requested document 2"

      }

    }
  }
}
