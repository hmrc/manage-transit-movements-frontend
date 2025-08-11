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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generated.*
import generators.Generators
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DepartureCancelledP5Helper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureCancelledP5HelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "DepartureCancelledP5HelperSpec" - {

    "buildMRNRow" - {

      "must return None" in {
        forAll(arbitrary[CC009CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(MRN = None))
        }) {
          message =>
            val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

            val result = helper.buildMRNRow

            result must not be defined
        }
      }

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          mrn =>
            forAll(arbitrary[CC009CType].map {
              x =>
                x.copy(TransitOperation = x.TransitOperation.copy(MRN = Some(mrn)))
            }) {
              message =>
                val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

                val result = helper.buildMRNRow

                result.value mustEqual SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value(mrn.toText))
            }
        }
      }
    }

    "buildDateTimeDecisionRow" - {

      "must return None" in {
        forAll(arbitrary[CC009CType].map {
          x =>
            x.copy(Invalidation = x.Invalidation.copy(decisionDateAndTime = None))
        }) {
          message =>
            val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

            val result = helper.buildDateTimeDecisionRow

            result must not be defined
        }
      }

      "must return SummaryListRow" in {
        val decisionDateAndTime = XMLCalendar("2014-06-09T16:15:04")
        forAll(arbitrary[CC009CType].map {
          x =>
            x.copy(Invalidation = x.Invalidation.copy(decisionDateAndTime = Some(decisionDateAndTime)))
        }) {
          message =>
            val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

            val result = helper.buildDateTimeDecisionRow

            result.value mustEqual SummaryListRow(key = Key("Date and time of decision".toText), value = Value("09 June 2014 at 4:15pm".toText))
        }
      }
    }

    "buildInitiatedByCustomsRow" - {

      "must return SummaryListRow" - {

        "with yes when InitiatedByCustoms is 1" in {
          forAll(arbitrary[CC009CType].map {
            x =>
              x.copy(Invalidation = x.Invalidation.copy(initiatedByCustoms = Number1))
          }) {
            message =>
              val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

              val result = helper.buildInitiatedByCustomsRow

              result.value mustEqual SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("Yes".toText))
          }
        }

        "with no when InitiatedByCustoms is 0" in {
          forAll(arbitrary[CC009CType].map {
            x =>
              x.copy(Invalidation = x.Invalidation.copy(initiatedByCustoms = Number0))
          }) {
            message =>
              val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

              val result = helper.buildInitiatedByCustomsRow

              result.value mustEqual SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("No".toText))
          }
        }
      }
    }

    "buildOfficeOfDepartureRow" - {

      "must return SummaryListRow with office name and code" in {
        forAll(Gen.alphaNumStr) {
          customsOfficeId =>
            forAll(arbitrary[CC009CType].map {
              x =>
                x.copy(CustomsOfficeOfDeparture = x.CustomsOfficeOfDeparture.copy(referenceNumber = customsOfficeId))
            }) {
              message =>
                when(mockReferenceDataService.getCustomsOffice(eqTo(customsOfficeId))(any(), any()))
                  .thenReturn(Future.successful(CustomsOffice("GB00060", "BOSTON", None, None)))

                val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

                val result = helper.buildOfficeOfDepartureRow.futureValue

                result.value mustEqual SummaryListRow(key = Key("Office of departure".toText), value = Value("BOSTON (GB00060)".toText))
            }
        }
      }

      "must throw an exception" - {
        "when customs office returns None" in {
          forAll(arbitrary[CC009CType]) {
            message =>
              val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

              when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
                .thenReturn(Future.failed(new NoReferenceDataFoundException("")))

              val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

              whenReady(helper.buildOfficeOfDepartureRow.failed) {
                result => result mustBe a[NoReferenceDataFoundException]
              }
          }
        }
      }
    }

    "buildCommentsRow" - {

      "must return None" in {
        forAll(arbitrary[CC009CType].map {
          x =>
            x.copy(Invalidation = x.Invalidation.copy(justification = None))
        }) {
          message =>
            val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

            val result = helper.buildCommentsRow

            result must not be defined
        }
      }

      "must return SummaryListRow" in {
        forAll(Gen.alphaNumStr) {
          justification =>
            forAll(arbitrary[CC009CType].map {
              x =>
                x.copy(Invalidation = x.Invalidation.copy(justification = Some(justification)))
            }) {
              message =>
                val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

                val result = helper.buildCommentsRow

                result.value mustEqual SummaryListRow(key = Key("Comments".toText), value = Value(justification.toText))
            }
        }
      }
    }

    "buildInvalidationSection" - {

      "must return a Section" in {
        forAll(arbitrary[CC009CType].map {
          x =>
            x
              .copy(TransitOperation = x.TransitOperation.copy(MRN = Some("abd123")))
              .copy(Invalidation =
                x.Invalidation.copy(
                  decisionDateAndTime = Some(XMLCalendar("2014-06-09T16:15:04")),
                  initiatedByCustoms = Number1,
                  justification = Some("some justification")
                )
              )
              .copy(CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType05("22323323"))
        }) {
          message =>
            when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

            val helper = new DepartureCancelledP5Helper(message, mockReferenceDataService)

            val result = helper.buildInvalidationSection.futureValue

            result.sectionTitle must not be defined

            result.rows.head mustEqual SummaryListRow(key = Key("Movement Reference Number (MRN)".toText), value = Value("abd123".toText))
            result.rows(1) mustEqual SummaryListRow(key = Key("Date and time of decision".toText), value = Value("09 June 2014 at 4:15pm".toText))
            result.rows(2) mustEqual SummaryListRow(key = Key("Initiated by Customs?".toText), value = Value("Yes".toText))
            result.rows(3) mustEqual SummaryListRow(key = Key("Office of departure".toText), value = Value("Customs Office (1234)".toText))
            result.rows(4) mustEqual SummaryListRow(key = Key("Comments".toText), value = Value("some justification".toText))
        }
      }
    }
  }
}
