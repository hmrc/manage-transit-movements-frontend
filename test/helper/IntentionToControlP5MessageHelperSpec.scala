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
import models.referenceData.CustomsOffice
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
import utils.IntentionToControlP5MessageHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IntentionToControlP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "IntentionToControlP5MessageHelper" - {
    "buildLRNRow" - {
      "must return None" in {
        forAll(arbitrary[CC060CType].map {
          x =>
            x.copy(TransitOperation = x.TransitOperation.copy(LRN = None))
        }) {
          message =>
            val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

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
                val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

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
            val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

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
                val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

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
            val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

            val result = helper.buildDateTimeControlRow

            result mustBe
              Some(SummaryListRow(key = Key("Date and time of control notification".toText), value = Value("09 June 2014 at 4:15pm".toText)))
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

                  val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

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

              val helper = new IntentionToControlP5MessageHelper(message, mockReferenceDataService)

              whenReady(helper.buildOfficeOfDepartureRow.failed) {
                result => result mustBe a[NoReferenceDataFoundException]
              }
          }
        }
      }
    }
  }
}
