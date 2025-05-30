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
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generated.*
import generators.Generators
import models.referenceData.{ControlType, CustomsOffice}
import models.referenceData.RequestedDocumentType as RequestedDocumentTypeRef
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section.StaticSection

import scala.concurrent.ExecutionContext.Implicits.global
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
        forAll(arbitrary[CC060CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(LRN = None))
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.buildLRNRow

            result `mustBe` None
        }
      }

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          lrn =>
            forAll(arbitrary[CC060CType].map {
              x =>
                x.copy(TransitOperation = x.TransitOperation.copy(LRN = Some(lrn)))
            }) {
              message =>
                val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

                val result = helper.buildLRNRow

                result mustBe
                  Some(SummaryListRow(key = Key("Local Reference Number (LRN)".toText), value = Value(lrn.toText)))
            }
        }
      }
    }

    "buildMRNRow" - {
      "must return None" in {
        forAll(arbitrary[CC060CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(MRN = None))
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.buildMRNRow

            result `mustBe` None
        }
      }

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          mrn =>
            forAll(arbitrary[CC060CType].map {
              x =>
                x.copy(TransitOperation = x.TransitOperation.copy(MRN = Some(mrn)))
            }) {
              message =>
                val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

                val result = helper.buildMRNRow

                result mustBe
                  Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value(mrn.toText)))
            }
        }
      }
    }

    "buildDateTimeControllerRow" - {

      "must return SummaryListRow" in {
        val controlNotificationDateAndTime = XMLCalendar("2014-06-09T16:15:04")

        forAll(arbitrary[CC060CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(controlNotificationDateAndTime = controlNotificationDateAndTime))
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.buildDateTimeControlRow

            result mustBe
              Some(SummaryListRow(key = Key("Date and time of control".toText), value = Value("09 June 2014 at 4:15pm".toText)))
        }
      }
    }

    "buildOfficeOfDepartureRow" - {

      "must return SummaryListRow with customs office id and code" - {
        "when reference data call returns a customs office" in {
          forAll(Gen.alphaNumStr) {
            customsOfficeId =>
              forAll(arbitrary[CC060CType].map {
                x =>
                  x.copy(CustomsOfficeOfDeparture = x.CustomsOfficeOfDeparture.copy(referenceNumber = customsOfficeId))
              }) {
                message =>
                  when(mockReferenceDataService.getCustomsOffice(eqTo(customsOfficeId))(any(), any()))
                    .thenReturn(Future.successful(CustomsOffice("22323323", "Office", None, None)))

                  val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

                  val result = helper.buildOfficeOfDepartureRow.futureValue

                  result mustBe
                    Some(SummaryListRow(key = Key("Office of departure".toText), value = Value("Office (22323323)".toText)))
              }
          }
        }
      }

      "must throw an exception" - {
        "when reference data call returns None" in {
          forAll(arbitrary[CC060CType]) {
            message =>
              val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

              when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
                .thenReturn(Future.failed(new NoReferenceDataFoundException("")))

              val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

              whenReady(helper.buildOfficeOfDepartureRow.failed) {
                result => result mustBe a[NoReferenceDataFoundException]
              }
          }
        }
      }
    }

    "controlInformationSection" - {

      "must return empty Sequence of Sections" in {
        forAll(arbitrary[CC060CType].map {
          _.copy(TypeOfControls = Nil)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.controlInformationSection().futureValue

            result `mustBe` Seq.empty
        }
      }

      "must return Sequence of Sections when one typeOfControl is found in referenceData" in {

        val controlType44 = ControlType("44", "Nature and characteristics of the goods")
        val controlType45 = ControlType("45", "")

        val typeOfControls = Seq(
          TypeOfControlsType(1, "44", None),
          TypeOfControlsType(2, "45", Some("Desc1"))
        )

        when(mockReferenceDataService.getControlType("44")).thenReturn(Future.successful(controlType44))
        when(mockReferenceDataService.getControlType("45")).thenReturn(Future.successful(controlType45))

        forAll(arbitrary[CC060CType].map {
          _.copy(TypeOfControls = typeOfControls)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.controlInformationSection().futureValue

            val firstRow =
              Seq(SummaryListRow(key = Key("Type".toText), value = Value("44 - Nature and characteristics of the goods".toText)))

            val secondRow = Seq(
              SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
              SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
            )

            val seqSummaryRow = Seq(StaticSection(Some("Control information 1"), firstRow), StaticSection(Some("Control information 2"), secondRow))

            result `mustBe` seqSummaryRow
        }
      }

      "must return Sequence of Sections when no typeOfControl is found in referenceData" in {

        val controlType44 = ControlType("44", "")
        val controlType45 = ControlType("45", "")

        val typeOfControls = Seq(
          TypeOfControlsType(1, "44", None),
          TypeOfControlsType(2, "45", Some("Desc1"))
        )

        when(mockReferenceDataService.getControlType("44")).thenReturn(Future.successful(controlType44))
        when(mockReferenceDataService.getControlType("45")).thenReturn(Future.successful(controlType45))

        forAll(arbitrary[CC060CType].map {
          _.copy(TypeOfControls = typeOfControls)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.controlInformationSection().futureValue

            val firstRow =
              Seq(SummaryListRow(key = Key("Type".toText), value = Value("44".toText)))

            val secondRow = Seq(
              SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
              SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
            )

            val seqSummaryRow = Seq(StaticSection(Some("Control information 1"), firstRow), StaticSection(Some("Control information 2"), secondRow))

            result `mustBe` seqSummaryRow
        }
      }
    }

    "documentSection" - {

      "must return empty Sequence of Sections" in {
        forAll(arbitrary[CC060CType].map {
          _.copy(RequestedDocument = Nil)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.documentSection().futureValue

            result `mustBe` Seq.empty
        }
      }

      "must return Sequence of Sections when RequestedDocumentType is found in referenceData" in {

        val requestedDocument1: RequestedDocumentTypeRef = RequestedDocumentTypeRef("C605", "Information sheet INF3")

        val requestedDocument2: RequestedDocumentTypeRef = RequestedDocumentTypeRef("C620", "T2LF document")

        val requestedDocuments = Seq(
          RequestedDocumentType(1, "C605", None),
          RequestedDocumentType(2, "C620", Some("Desc1"))
        )

        when(mockReferenceDataService.getRequestedDocumentType("C605")).thenReturn(Future.successful(requestedDocument1))
        when(mockReferenceDataService.getRequestedDocumentType("C620")).thenReturn(Future.successful(requestedDocument2))

        forAll(arbitrary[CC060CType].map {
          _.copy(RequestedDocument = requestedDocuments)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.documentSection().futureValue

            val firstRow =
              Seq(SummaryListRow(key = Key("Type".toText), value = Value("C605 - Information sheet INF3".toText)))

            val secondRow = Seq(
              SummaryListRow(key = Key("Type".toText), value = Value("C620 - T2LF document".toText)),
              SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
            )

            val seqSummaryRow = Seq(StaticSection(Some("Requested document 1"), firstRow), StaticSection(Some("Requested document 2"), secondRow))

            result `mustBe` seqSummaryRow
        }
      }

      "must return Sequence of Sections when RequestedDocumentType is not found in referenceData" in {

        val requestedDocument1: RequestedDocumentTypeRef = RequestedDocumentTypeRef("C605", "")

        val requestedDocument2: RequestedDocumentTypeRef = RequestedDocumentTypeRef("C620", "")

        val requestedDocuments = Seq(
          RequestedDocumentType(1, "C605", None),
          RequestedDocumentType(2, "C620", Some("Desc1"))
        )

        when(mockReferenceDataService.getRequestedDocumentType("C605")).thenReturn(Future.successful(requestedDocument1))
        when(mockReferenceDataService.getRequestedDocumentType("C620")).thenReturn(Future.successful(requestedDocument2))

        forAll(arbitrary[CC060CType].map {
          _.copy(RequestedDocument = requestedDocuments)
        }) {
          message =>
            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.documentSection().futureValue

            val firstRow =
              Seq(SummaryListRow(key = Key("Type".toText), value = Value("C605".toText)))

            val secondRow = Seq(
              SummaryListRow(key = Key("Type".toText), value = Value("C620".toText)),
              SummaryListRow(key = Key("Description".toText), value = Value("Desc1".toText))
            )

            val seqSummaryRow = Seq(StaticSection(Some("Requested document 1"), firstRow), StaticSection(Some("Requested document 2"), secondRow))

            result `mustBe` seqSummaryRow
        }
      }

    }

    "buildGoodsUnderControlSection" - {

      "must return a Section" in {
        forAll(arbitrary[CC060CType].map {
          x =>
            x
              .copy(TransitOperation =
                x.TransitOperation.copy(
                  LRN = Some("LRN1"),
                  MRN = Some("MRN1"),
                  controlNotificationDateAndTime = XMLCalendar("2014-06-09T16:15:04")
                )
              )
              .copy(CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03("22323323"))
        }) {
          message =>
            when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

            val helper = new GoodsUnderControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.buildGoodsUnderControlSection().futureValue

            result.sectionTitle must not be defined

            result.rows.head `mustBe` SummaryListRow(key = Key("Local Reference Number (LRN)".toText), value = Value("LRN1".toText))
            result.rows(1) `mustBe` SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("MRN1".toText))
            result.rows(2) `mustBe` SummaryListRow(key = Key("Date and time of control".toText), value = Value("09 June 2014 at 4:15pm".toText))
            result.rows(3) `mustBe` SummaryListRow(key = Key("Office of departure".toText), value = Value("Customs Office (1234)".toText))
        }
      }
    }
  }

}
