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
import generated._
import generators.Generators
import models.referenceData.InvalidGuaranteeReason
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import utils.GuaranteeRejectedP5Helper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GuaranteeRejectedP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "RejectionMessageP5MessageHelper" - {

    "buildErrorCodeRow" - {

      "must return SummaryListRow" - {

        "when description present in reference data" in {
          forAll(arbitraryGuaranteeReferenceType08.arbitrary.sample.value, arbitraryInvalidGuaranteeReasonType01.arbitrary.sample.value, nonEmptyString) {
            (guaranteeReference, invalidReason, description) =>
              forAll(arbitrary[CC055CType].map {
                _.copy(GuaranteeReference =
                  Seq(
                    GuaranteeReferenceType08(
                      GRN = guaranteeReference.GRN,
                      sequenceNumber = guaranteeReference.sequenceNumber,
                      InvalidGuaranteeReason = Seq(
                        InvalidGuaranteeReasonType01(
                          sequenceNumber = invalidReason.sequenceNumber,
                          code = invalidReason.code,
                          text = invalidReason.text
                        )
                      )
                    )
                  )
                )
              }) {
                message =>
                  val invalidGuaranteeReason = InvalidGuaranteeReason(invalidReason.toString, description)

                  when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo(invalidReason.toString))(any(), any()))
                    .thenReturn(Future.successful(invalidGuaranteeReason))

                  val helper = new GuaranteeRejectedP5Helper(message.GuaranteeReference, mockReferenceDataService)

                  val result = helper.tables.futureValue

                  result mustBe Seq(
                    Seq(
                      TableRow(Text(s"${invalidReason.toString} - $description")),
                      TableRow(Text(s"${invalidReason.text}"))
                    )
                  )
              }
          }
        }
      }
    }
  }
}
