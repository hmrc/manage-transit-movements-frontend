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
import generated.{FunctionalErrorType04, Number12}
import models.FunctionalError.{FunctionalErrorWithSection, FunctionalErrorWithoutSection}
import models.FunctionalErrors.{FunctionalErrorsWithSection, FunctionalErrorsWithoutSection}
import models.InvalidDataItem
import models.referenceData.FunctionalErrorWithDesc
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
      "must convert a series of FunctionalErrorType04 values to FunctionalErrorsWithSection" in {
        val input = Seq(
          FunctionalErrorType04(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
            errorReason = "BR20004",
            originalAttributeValue = Some("GB635733627000")
          ),
          FunctionalErrorType04(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
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

        result mustBe expectedResult
      }
    }

    "convertErrorsWithoutSection" - {
      "must convert a series of FunctionalErrorType04 values to FunctionalErrorsWithoutSection" in {
        val input = Seq(
          FunctionalErrorType04(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
            errorReason = "BR20004",
            originalAttributeValue = Some("GB635733627000")
          ),
          FunctionalErrorType04(
            errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
            errorCode = Number12,
            errorReason = "BR20005",
            originalAttributeValue = None
          )
        )

        val output = FunctionalErrorsWithoutSection(
          Seq(
            FunctionalErrorWithoutSection(
              error = "12",
              businessRuleId = "BR20004",
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = Some("GB635733627000")
            ),
            FunctionalErrorWithoutSection(
              error = "14",
              businessRuleId = "BR20005",
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = None
            )
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

        result mustBe expectedResult
      }
    }
  }
}
