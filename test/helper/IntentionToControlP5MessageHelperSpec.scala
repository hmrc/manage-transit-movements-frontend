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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.IntentionToControlP5MessageHelper
import viewModels.sections.Section

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class IntentionToControlP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "IntentionToControlP5MessageHelper" - {
    "buildLRNRow" - {
      "must return None" in {
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.buildLRNRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, Some("LRN001"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.buildLRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Local Reference Number (LRN)".toText), value = Value("LRN001".toText)))
      }
    }

    "buildMRNRow" - {
      "must return None" in {
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.buildMRNRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(Some("MRN001"), None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.buildMRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("MRN001".toText)))
      }
    }

    "buildDateTimeControllerRow" - {

      "must return SummaryListRow" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.buildDateTimeControlRow

        result mustBe
          Some(SummaryListRow(key = Key("Date and time of control notification".toText), value = Value("09 June 2014 at 4:15 pm".toText)))
      }
    }

    "documentSection" - {

      "must return empty Sequence of Sections" in {

        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification2"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            None
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.documentSection()

        result mustBe Seq.empty

      }

      "must return Sequence of Sections" in {

        val requestedDocument = Some(Seq(RequestedDocument("1", "44", None), RequestedDocument("2", "45", Some("Desc1"))))
        val message: IE060Data = IE060Data(
          IE060MessageData(
            TransitOperationIE060(None, None, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            None,
            requestedDocument
          )
        )

        val helper = new IntentionToControlP5MessageHelper(message.data)

        val result = helper.documentSection()

        val firstRow =
          Seq(SummaryListRow(key = Key("Type".toText), value = Value("44".toText)),
              SummaryListRow(key = Key("Reference number".toText), value = Value("22323323".toText))
          )

        val secondRow = Seq(
          SummaryListRow(key = Key("Type".toText), value = Value("45".toText)),
          SummaryListRow(key = Key("Reference number".toText), value = Value("22323323".toText))
        )

        val seqSummaryRow = Seq(Section(Some("Control information 1"), firstRow, None), Section(Some("Control information 2"), secondRow, None))

        result mustBe seqSummaryRow

      }
    }
  }
}
