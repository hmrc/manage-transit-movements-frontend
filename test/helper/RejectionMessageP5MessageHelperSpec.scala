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
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.RejectionMessageP5MessageHelper
import viewModels.sections.Section

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RejectionMessageP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  val lrnString = "LRNAB123"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "RejectionMessageP5MessageHelper" - {

    "buildErrorCodeRow" - {

      val code1            = "12"
      val codeDescription1 = "Codelist violation"
      val code2            = "13"
      val codeDescription2 = "Codelist violation2"

      "must return SummaryListRow" - {

        "when description present in reference data" in {
          val functionalErrorReferenceData = Seq(FunctionalErrorWithDesc(code1, codeDescription1), FunctionalErrorWithDesc(code2, codeDescription2))

          val message: IE056Data = IE056Data(
            IE056MessageData(
              TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
              CustomsOfficeOfDeparture("AB123"),
              Seq(FunctionalError("14", code1, "MRN incorrect", None))
            )
          )

          when(mockReferenceDataService.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

          val helper = new RejectionMessageP5MessageHelper(message.data.functionalErrors, mockReferenceDataService)

          val result = helper.buildErrorCodeRow(code1).futureValue

          result mustBe
            Some(SummaryListRow(key = Key("Error".toText), value = Value(s"$code1 - $codeDescription1".toText)))
        }

        "when description not present in reference data" in {
          val functionalErrorReferenceData = FunctionalErrorWithDesc(code1, "")

          val message: IE056Data = IE056Data(
            IE056MessageData(
              TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
              CustomsOfficeOfDeparture("AB123"),
              Seq(FunctionalError("14", "12", "MRN incorrect", None))
            )
          )

          when(mockReferenceDataService.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(Seq(functionalErrorReferenceData)))

          val helper = new RejectionMessageP5MessageHelper(message.data.functionalErrors, mockReferenceDataService)

          val result = helper.buildErrorCodeRow(code1).futureValue

          result mustBe
            Some(SummaryListRow(key = Key("Error".toText), value = Value(code1.toText)))
        }
      }
    }

    "buildErrorReasonRow" - {
      "must return SummaryListRow" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
            CustomsOfficeOfDeparture("AB123"),
            Seq(FunctionalError("14", "12", "MRN incorrect", None))
          )
        )

        val helper = new RejectionMessageP5MessageHelper(message.data.functionalErrors, mockReferenceDataService)

        val result = helper.buildErrorReasonRow("MRN incorrect")

        result mustBe
          Some(SummaryListRow(key = Key("Reason".toText), value = Value("MRN incorrect".toText)))
      }
    }

    "buildErrorRows" - {
      "must return sequence of summaryListRow when errors present" in {

        val functionalErrors = Seq(FunctionalError("1", "12", "Codelist violation", None))

        val functionalErrorReferenceData = Seq(FunctionalErrorWithDesc("12", "MRN Invalid"))

        when(mockReferenceDataService.getFunctionalErrors()).thenReturn(Future.successful(functionalErrorReferenceData))

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
            CustomsOfficeOfDeparture("AB123"),
            functionalErrors
          )
        )

        val helper = new RejectionMessageP5MessageHelper(message.data.functionalErrors, mockReferenceDataService)

        val result = helper.buildErrorRows(functionalErrors.head).futureValue

        val firstRow =
          SummaryListRow(key = Key("Error".toText), value = Value("12 - MRN Invalid".toText))

        val secondRow =
          SummaryListRow(key = Key("Reason".toText), value = Value("Codelist violation".toText))

        firstRow mustBe result.head
        secondRow mustBe result(1)

        val seqSummaryRow = Seq(firstRow, secondRow)

        result mustBe seqSummaryRow

      }
    }

    "errorSection" - {
      "must return a section of errors when errors present" in {

        val functionalErrors = Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("1", "14", "Invalid declaration", None))

        val functionalErrorReferenceData1 = FunctionalErrorWithDesc("12", "MRN Invalid")
        val functionalErrorReferenceData2 = FunctionalErrorWithDesc("14", "Rule Violation")

        val functionalErrorReferenceData = Seq(functionalErrorReferenceData1, functionalErrorReferenceData2)

        when(mockReferenceDataService.getFunctionalErrors()).thenReturn(Future.successful(functionalErrorReferenceData))

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
            CustomsOfficeOfDeparture("AB123"),
            functionalErrors
          )
        )

        val helper = new RejectionMessageP5MessageHelper(message.data.functionalErrors, mockReferenceDataService)

        val result = helper.errorSection().futureValue

        val firstRow =
          SummaryListRow(key = Key("Error".toText), value = Value("12 - MRN Invalid".toText))

        val secondRow =
          SummaryListRow(key = Key("Reason".toText), value = Value("Codelist violation".toText))

        val thirdRow =
          SummaryListRow(key = Key("Error".toText), value = Value("14 - Rule Violation".toText))

        val fourthRow =
          SummaryListRow(key = Key("Reason".toText), value = Value("Invalid declaration".toText))

        firstRow mustBe result.rows.head
        secondRow mustBe result.rows(1)
        thirdRow mustBe result.rows(2)
        fourthRow mustBe result.rows(3)

        val seqSummaryRows = Seq(firstRow, secondRow, thirdRow, fourthRow)

        result.rows mustBe seqSummaryRows

        result mustBe Section(None, seqSummaryRows, None)

      }
    }

  }
}
