/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.DepartureCacheConnector
import generated.*
import models.FunctionalError.{FunctionalErrorWithSection, FunctionalErrorWithoutSection}
import models.FunctionalErrors.{FunctionalErrorsWithSection, FunctionalErrorsWithoutSection}
import models.referenceData.{FunctionalErrorWithDesc, InvalidGuaranteeReason}
import models.{FunctionalErrorType, GuaranteeReference, InvalidDataItem}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FunctionalErrorsServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockDepartureCacheConnector = mock[DepartureCacheConnector]

  private val mockReferenceDataService = mock[ReferenceDataService]

  private val service = app.injector.instanceOf[FunctionalErrorsService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureCacheConnector].toInstance(mockDepartureCacheConnector),
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureCacheConnector)
    reset(mockReferenceDataService)
  }

  "FunctionalErrorsService" - {
    "convertErrorsWithSection" - {
      "must convert a series of FunctionalErrorType07 values to FunctionalErrorsWithSection" in {
        val input = Seq(
          FunctionalErrorType(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12.toString,
            errorReason = "BR20004",
            originalAttributeValue = Some("GB635733627000")
          ),
          FunctionalErrorType(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number14.toString,
            errorReason = "BR20005",
            originalAttributeValue = None
          )
        )

        val output = FunctionalErrorsWithSection(
          Seq(
            FunctionalErrorWithSection(
              error = "12",
              businessRuleId = "BR20004",
              section = Some("Trader details"),
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = Some("GB635733627000")
            ),
            FunctionalErrorWithSection(
              error = "14",
              businessRuleId = "BR20005",
              section = None,
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = None
            )
          )
        )

        val expectedResult = FunctionalErrorsWithSection(
          Seq(
            FunctionalErrorWithSection(
              error = "12 - foo",
              businessRuleId = "BR20004",
              section = Some("Trader details"),
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = Some("GB635733627000")
            ),
            FunctionalErrorWithSection(
              error = "14 - bar",
              businessRuleId = "BR20005",
              section = None,
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = None
            )
          )
        )

        when(mockDepartureCacheConnector.convertErrors(any())(any()))
          .thenReturn(Future.successful(output))

        when(mockReferenceDataService.getFunctionalError(eqTo("12"))(any(), any()))
          .thenReturn(Future.successful(FunctionalErrorWithDesc("12", "foo")))

        when(mockReferenceDataService.getFunctionalError(eqTo("14"))(any(), any()))
          .thenReturn(Future.successful(FunctionalErrorWithDesc("14", "bar")))

        val result = service.convertErrorsWithSection(input).futureValue

        result mustEqual expectedResult
      }
    }

    "convertErrorsWithoutSection" - {
      "must convert a series of FunctionalErrorType07 values to FunctionalErrorsWithoutSection" in {
        val input = Seq(
          FunctionalErrorType(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12.toString,
            errorReason = "BR20004",
            originalAttributeValue = Some("GB635733627000")
          ),
          FunctionalErrorType(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number14.toString,
            errorReason = "BR20005",
            originalAttributeValue = None
          )
        )

        val expectedResult = FunctionalErrorsWithoutSection(
          Seq(
            FunctionalErrorWithoutSection(
              error = "12 - foo",
              businessRuleId = "BR20004",
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = Some("GB635733627000")
            ),
            FunctionalErrorWithoutSection(
              error = "14 - bar",
              businessRuleId = "BR20005",
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = None
            )
          )
        )

        when(mockReferenceDataService.getFunctionalError(eqTo("12"))(any(), any()))
          .thenReturn(Future.successful(FunctionalErrorWithDesc("12", "foo")))

        when(mockReferenceDataService.getFunctionalError(eqTo("14"))(any(), any()))
          .thenReturn(Future.successful(FunctionalErrorWithDesc("14", "bar")))

        val result = service.convertErrorsWithoutSection(input).futureValue

        result mustEqual expectedResult
      }
    }

    "convertGuaranteeReferences" - {
      "must convert a series of GuaranteeReferenceType07 values to a series of GuaranteeReference" in {
        val input = Seq(
          GuaranteeReferenceType07(
            sequenceNumber = 1,
            GRN = "GRN 1",
            InvalidGuaranteeReason = Seq(
              InvalidGuaranteeReasonType01(
                sequenceNumber = 1,
                code = "Code 1_1",
                text = Some("Text 1_1")
              ),
              InvalidGuaranteeReasonType01(
                sequenceNumber = 2,
                code = "Code 1_2",
                text = Some("Text 1_2")
              )
            )
          ),
          GuaranteeReferenceType07(
            sequenceNumber = 2,
            GRN = "GRN 2",
            InvalidGuaranteeReason = Seq(
              InvalidGuaranteeReasonType01(
                sequenceNumber = 1,
                code = "Code 2_1",
                text = Some("Text 2_1")
              ),
              InvalidGuaranteeReasonType01(
                sequenceNumber = 2,
                code = "Code 2_2",
                text = Some("Text 2_2")
              )
            )
          )
        )

        val expectedResult = Seq(
          GuaranteeReference(
            grn = "GRN 1",
            invalidGuarantees = Seq(
              models.InvalidGuaranteeReason(
                error = "Code 1_1 - Description 1_1",
                furtherInformation = Some("Text 1_1")
              ),
              models.InvalidGuaranteeReason(
                error = "Code 1_2 - Description 1_2",
                furtherInformation = Some("Text 1_2")
              )
            )
          ),
          GuaranteeReference(
            grn = "GRN 2",
            invalidGuarantees = Seq(
              models.InvalidGuaranteeReason(
                error = "Code 2_1 - Description 2_1",
                furtherInformation = Some("Text 2_1")
              ),
              models.InvalidGuaranteeReason(
                error = "Code 2_2 - Description 2_2",
                furtherInformation = Some("Text 2_2")
              )
            )
          )
        )

        when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("Code 1_1"))(any(), any()))
          .thenReturn(Future.successful(InvalidGuaranteeReason("Code 1_1", "Description 1_1")))

        when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("Code 1_2"))(any(), any()))
          .thenReturn(Future.successful(InvalidGuaranteeReason("Code 1_2", "Description 1_2")))

        when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("Code 2_1"))(any(), any()))
          .thenReturn(Future.successful(InvalidGuaranteeReason("Code 2_1", "Description 2_1")))

        when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("Code 2_2"))(any(), any()))
          .thenReturn(Future.successful(InvalidGuaranteeReason("Code 2_2", "Description 2_2")))

        val result = service.convertGuaranteeReferences(input).futureValue

        result mustEqual expectedResult
      }
    }
  }
}
