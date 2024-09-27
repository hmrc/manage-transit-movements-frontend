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
import models.departureP5.GuaranteeReferenceTable
import models.referenceData.InvalidGuaranteeReason
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import utils.GuaranteeRejectedP5Helper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GuaranteeRejectedP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "GuaranteeRejectedP5Helper" - {

    "buildErrorCodeRow" - {

      "must return SummaryListRow" - {

        "using the description present in reference data" in {
          forAll(arbitraryGuaranteeReferenceType08.arbitrary.sample.value, nonEmptyString) {
            (guaranteeReference, text) =>
              val invalidReason1 = InvalidGuaranteeReasonType01(1, "G02", Some(text))
              val invalidReason2 = InvalidGuaranteeReasonType01(2, "G03", Some(text))
              forAll(arbitrary[CC055CType].map {
                _.copy(GuaranteeReference =
                  Seq(
                    GuaranteeReferenceType08(
                      GRN = guaranteeReference.GRN,
                      sequenceNumber = guaranteeReference.sequenceNumber,
                      InvalidGuaranteeReason = Seq(invalidReason1, invalidReason2)
                    ),
                    GuaranteeReferenceType08(
                      GRN = guaranteeReference.GRN,
                      sequenceNumber = guaranteeReference.sequenceNumber,
                      InvalidGuaranteeReason = Seq(invalidReason1, invalidReason2)
                    )
                  )
                )
              }) {
                message =>
                  val invalidGuaranteeReason1 = InvalidGuaranteeReason(invalidReason1.code, "Guarantee exists, but not valid")
                  val invalidGuaranteeReason2 = InvalidGuaranteeReason(invalidReason2.code, "Access code not valid")

                  when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo(invalidReason1.code))(any(), any()))
                    .thenReturn(Future.successful(invalidGuaranteeReason1))

                  when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo(invalidReason2.code))(any(), any()))
                    .thenReturn(Future.successful(invalidGuaranteeReason2))

                  val helper = new GuaranteeRejectedP5Helper(message.GuaranteeReference, mockReferenceDataService)

                  val result = helper.tables.futureValue

                  result.head `mustBe` a[GuaranteeReferenceTable]
                  result.head.title `mustBe` "Guarantee reference 1"
                  result.head.grn `mustBe` guaranteeReference.GRN
                  result.head.table.head `mustBe` Some(
                    Seq(
                      HeadCell(Text("Error")),
                      HeadCell(Text("Further information"))
                    )
                  )
                  result.head.table.rows `mustBe` Seq(
                    Seq(
                      TableRow(Text(s"${invalidGuaranteeReason1.toString}")),
                      TableRow(Text(text))
                    ),
                    Seq(
                      TableRow(Text(s"${invalidGuaranteeReason2.toString}")),
                      TableRow(Text(text))
                    )
                  )

                  result(1) `mustBe` a[GuaranteeReferenceTable]
                  result(1).title `mustBe` "Guarantee reference 2"
                  result(1).grn `mustBe` guaranteeReference.GRN
                  result(1).table.head `mustBe` Some(
                    Seq(
                      HeadCell(Text("Error")),
                      HeadCell(Text("Further information"))
                    )
                  )
                  result(1).table.rows `mustBe` Seq(
                    Seq(
                      TableRow(Text(s"${invalidGuaranteeReason1.toString}")),
                      TableRow(Text(text))
                    ),
                    Seq(
                      TableRow(Text(s"${invalidGuaranteeReason2.toString}")),
                      TableRow(Text(text))
                    )
                  )
              }
          }
        }
      }
    }
  }

}
