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
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import utils.RejectionMessageP5MessageHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RejectionMessageP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "RejectionMessageP5MessageHelper" - {

    "buildErrorCodeRow" - {

      "must return SummaryListRow" - {

        "when description present in reference data" in {
          forAll(Gen.oneOf(AesNctsP5FunctionalErrorCodes.values), nonEmptyString) {
            (errorCode, errorDescription) =>
              forAll(arbitrary[CC056CType].map {
                _.copy(FunctionalError =
                  Seq(
                    FunctionalErrorType04(
                      errorPointer = "/CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber",
                      errorCode = errorCode,
                      errorReason = "MRN incorrect",
                      originalAttributeValue = None
                    )
                  )
                )
              }) {
                message =>
                  val functionalError = FunctionalErrorWithDesc(errorCode.toString, errorDescription)

                  when(mockReferenceDataService.getFunctionalError(eqTo(errorCode.toString))(any(), any()))
                    .thenReturn(Future.successful(functionalError))

                  val helper = new RejectionMessageP5MessageHelper(message.FunctionalError, mockReferenceDataService)

                  val result = helper.tableRows().futureValue

                  result mustBe Seq(
                    Seq(
                      TableRow(Text(s"${errorCode.toString} - $errorDescription")),
                      TableRow(Text("MRN incorrect")),
                      TableRow(Text("/CC015C/HolderOfTheTransitProcedure/TIRHolderIdentificationNumber")),
                      TableRow(Text("N/A"))
                    )
                  )
              }
          }
        }
      }
    }
  }
}
