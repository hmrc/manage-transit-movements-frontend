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

package helper

import base.SpecBase
import generators.Generators
import models.departureP5._
import models.referenceData.{ControlType, CustomsOffice}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class GoodsUnderControlP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "GoodsUnderControlP5MessageHelper" - {
    "buildLRNRow" - {
      "must return None" in {
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildLRNRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, Some("LRN001"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildLRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Local Reference Number (LRN)".toText), value = Value("LRN001".toText)))
      }
    }

    "buildMRNRow" - {
      "must return None" in {
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildMRNRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(Some("MRN001"), None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildMRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("MRN001".toText)))
      }
    }

    "buildDateTimeControllerRow" - {

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildDateTimeControlRow

        result mustBe
          Some(SummaryListRow(key = Key("Date and time of control".toText), value = Value("09 June 2014 at 4:15 pm".toText)))
      }
    }

    "buildOfficeOfDepartureRow" - {

      "must return SummaryListRow with customs office id and code" - {
        "when reference data call returns a customs office" in {

          val message: IE060Data = IE060Data(
            IE060MessageData(
              TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
              CustomsOfficeOfDeparture("22323323"),
              None,
              None
            )
          )

          when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any()))
            .thenReturn(Future.successful(Some(CustomsOffice("22323323", "Office", None))))

          val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

          val result = helper.buildOfficeOfDepartureRow.futureValue

          result mustBe
            Some(SummaryListRow(key = Key("Office of departure".toText), value = Value("Office (22323323)".toText)))
        }
      }

      "must return SummaryListRow with customs office code" - {
        "when reference data call returns None" in {

          val message: IE060Data = IE060Data(
            IE060MessageData(
              TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
              CustomsOfficeOfDeparture("22323323"),
              None,
              None
            )
          )

          when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any()))
            .thenReturn(Future.successful(None))

          val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

          val result = helper.buildOfficeOfDepartureRow.futureValue

          result mustBe
            Some(SummaryListRow(key = Key("Office of departure".toText), value = Value("22323323".toText)))
        }
      }
    }

    "controlInformationSection" - {

      "must return empty Sequence of Sections" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.controlInformationSection().futureValue

        result mustBe Seq.empty

      }

      "must return Sequence of Sections when one typeOfControl is found in referenceData" in {

        val typeOfControls = Some(Seq(TypeOfControls("1", "44", None), TypeOfControls("2", "45", Some("Desc1"))))
        val controlType44  = ControlType("44", "Nature and characteristics of the goods")
        val controlType45  = ControlType("45", "")

        when(mockReferenceDataService.getControlType("44")).thenReturn(Future.successful(controlType44))
        when(mockReferenceDataService.getControlType("45")).thenReturn(Future.successful(controlType45))

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            typeOfControls,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.controlInformationSection().futureValue

        val firstRow =
          Seq(SummaryListRow(key = Key("Type".toText), value = Value("44 - Nature and characteristics of the goods".toText)))

        val secondRow = Seq(
          SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
          SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
        )

        val seqSummaryRow = Seq(Section(Some("Control information 1"), firstRow, None), Section(Some("Control information 2"), secondRow, None))

        result mustBe seqSummaryRow

      }

      "must return Sequence of Sections when no typeOfControl is found in referenceData" in {

        val typeOfControls = Some(Seq(TypeOfControls("1", "44", None), TypeOfControls("2", "45", Some("Desc1"))))
        val controlType44  = ControlType("44", "")
        val controlType45  = ControlType("45", "")

        when(mockReferenceDataService.getControlType("44")).thenReturn(Future.successful(controlType44))
        when(mockReferenceDataService.getControlType("45")).thenReturn(Future.successful(controlType45))

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            typeOfControls,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.controlInformationSection().futureValue

        val firstRow =
          Seq(SummaryListRow(key = Key("Type".toText), value = Value("44".toText)))

        val secondRow = Seq(
          SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
          SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
        )

        val seqSummaryRow = Seq(Section(Some("Control information 1"), firstRow, None), Section(Some("Control information 2"), secondRow, None))

        result mustBe seqSummaryRow

      }
    }

    "documentSection" - {

      "must return empty Sequence of Sections" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.documentSection()

        result mustBe Seq.empty

      }

      "must return Sequence of Sections" in {

        val requestedDocument = Some(Seq(RequestedDocument("1", "44", None), RequestedDocument("2", "45", Some("Desc1"))))
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            requestedDocument
          )
        )

        when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(None))

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.documentSection()

        val firstRow =
          Seq(SummaryListRow(key = Key("Type".toText), value = Value("44".toText)))

        val secondRow = Seq(
          SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
          SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
        )

        val seqSummaryRow = Seq(Section(Some("Requested document 1"), firstRow, None), Section(Some("Requested document 2"), secondRow, None))

        result mustBe seqSummaryRow

      }
    }

    "buildGoodsUnderControlSection" - {

      "must return a Section" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperation(Some("MRN1"), Some("LRN1"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(None))

        val helper = new GoodsUnderControlP5MessageHelper(message.data, mockReferenceDataService)

        val result = helper.buildGoodsUnderControlSection().futureValue
        val firstRow =
          Seq(
            SummaryListRow(key = Key("Local Reference Number (LRN)".toText), value = Value("LRN1".toText)),
            SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("MRN1".toText)),
            SummaryListRow(key = Key("Date and time of control".toText), value = Value("09 June 2014 at 4:15 pm".toText)),
            SummaryListRow(key = Key("Office of departure".toText), value = Value("22323323".toText))
          )

        result mustBe Section(None, firstRow, None)

      }
    }
  }

}
