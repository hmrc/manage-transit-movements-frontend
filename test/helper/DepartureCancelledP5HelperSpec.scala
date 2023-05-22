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
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.DepartureCancelledP5Helper
import viewModels.sections.Section

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DepartureCancelledP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DepartureCancelledP5HelperSpec" - {

    "buildMRNRow" - {

      "must return None" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              None
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildMRNRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildMRNRow

        result mustBe
          Some(SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("abd123".toText)))
      }
    }

    "buildDateTimeDecisionRow" - {

      "must return None" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abc123")
            ),
            Invalidation(
              None,
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildDateTimeDecisionRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME)),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildDateTimeDecisionRow

        result mustBe
          Some(SummaryListRow(key = Key("Date and time of decision".toText), value = Value("09 June 2014 at 4:15 pm".toText)))
      }
    }

    "buildInitiatedByCustomsRow" - {

      "must return SummaryListRow" - {

        "with yes when InitiatedByCustoms is 1" in {

          val message: IE009Data = IE009Data(
            IE009MessageData(
              TransitOperationIE009(
                Some("abd123")
              ),
              Invalidation(
                Some(LocalDateTime.now()),
                Some("0"),
                "1",
                Some("some justification")
              ),
              CustomsOfficeOfDeparture(
                "1234"
              )
            )
          )

          val helper = new DepartureCancelledP5Helper(message.data)

          val result = helper.buildInitiatedByCustomsRow

          result mustBe
            Some(SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("Yes".toText)))
        }

        "with no when InitiatedByCustoms is 0" in {

          val message: IE009Data = IE009Data(
            IE009MessageData(
              TransitOperationIE009(
                Some("abd123")
              ),
              Invalidation(
                Some(LocalDateTime.now()),
                Some("0"),
                "0",
                Some("some justification")
              ),
              CustomsOfficeOfDeparture(
                "1234"
              )
            )
          )

          val helper = new DepartureCancelledP5Helper(message.data)

          val result = helper.buildInitiatedByCustomsRow

          result mustBe
            Some(SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("No".toText)))
        }
      }
    }

    "buildOfficeOfDepartureRow" - {

      "must return SummaryListRow" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildOfficeOfDepartureRow

        result mustBe
          Some(SummaryListRow(key = Key("Office of departure".toText), value = Value("1234".toText)))
      }
    }

    "buildCommentsRow" - {

      "must return None" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              Some("0"),
              "1",
              None
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildCommentsRow

        result mustBe None
      }

      "must return SummaryListRow" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildCommentsRow

        result mustBe
          Some(SummaryListRow(key = Key("Comments".toText), value = Value("some justification".toText)))
      }
    }

    "buildInvalidationSection" - {

      "must return a Section" in {

        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME)),
              Some("0"),
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              "1234"
            )
          )
        )

        val helper = new DepartureCancelledP5Helper(message.data)

        val result = helper.buildInvalidationSection
        val firstRow =
          Seq(
            SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("abd123".toText)),
            SummaryListRow(key = Key("Date and time of decision".toText), value = Value("09 June 2014 at 4:15 pm".toText)),
            SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("Yes".toText)),
            SummaryListRow(key = Key("Office of departure".toText), value = Value("1234".toText)),
            SummaryListRow(key = Key("Comments".toText), value = Value("some justification".toText))
          )

        result mustBe Section(None, firstRow, None)

      }
    }
  }

}
