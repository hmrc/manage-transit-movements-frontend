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
import generators.Generators
import models.LocalReferenceNumber
import models.departureP5.BusinessRejectionType._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class BusinessRejectionTypeServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockCacheConnector = mock[DepartureCacheConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheConnector))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheConnector)
  }

  "BusinessRejectionTypeService" - {

    "canProceedWithAmendment" - {
      "when 013 rejection" - {
        "must return true" - {
          "when data exists in cache for given LRN" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.doesDeclarationExist(any())(any()))
                  .thenReturn(Future.successful(true))

                val result = service.canProceedWithAmendment(AmendmentRejection, lrn.value, xPaths).futureValue
                result mustBe true

                verify(mockCacheConnector).doesDeclarationExist(eqTo(lrn.value))(any())
            }
          }
        }

        "must return false" - {
          "when data exists in cache for given LRN" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.doesDeclarationExist(any())(any()))
                  .thenReturn(Future.successful(false))

                val result = service.canProceedWithAmendment(AmendmentRejection, lrn.value, xPaths).futureValue
                result mustBe false

                verify(mockCacheConnector).doesDeclarationExist(eqTo(lrn.value))(any())
            }
          }
        }
      }

      "when 015 rejection" - {
        "must return true" - {
          "when declaration is amendable" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.isDeclarationAmendable(any(), any())(any()))
                  .thenReturn(Future.successful(true))

                val result = service.canProceedWithAmendment(DeclarationRejection, lrn.value, xPaths).futureValue
                result mustBe true

                verify(mockCacheConnector).isDeclarationAmendable(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }

        "must return false" - {
          "when declaration is not amendable" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.isDeclarationAmendable(any(), any())(any()))
                  .thenReturn(Future.successful(false))

                val result = service.canProceedWithAmendment(DeclarationRejection, lrn.value, xPaths).futureValue
                result mustBe false

                verify(mockCacheConnector).isDeclarationAmendable(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }
      }
    }

    "handleErrors" - {
      "when 013 rejection" - {
        "must return true" - {
          "when errors handled" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.handleAmendmentErrors(any(), any())(any()))
                  .thenReturn(Future.successful(true))

                val result = service.handleErrors(AmendmentRejection, lrn.value, xPaths).futureValue
                result mustBe true

                verify(mockCacheConnector).handleAmendmentErrors(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }

        "must return false" - {
          "when errors not handled" in {
            forAll(arbitrary[LocalReferenceNumber], arbitrary[Seq[String]]) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.handleAmendmentErrors(any(), any())(any()))
                  .thenReturn(Future.successful(false))

                val result = service.handleErrors(AmendmentRejection, lrn.value, xPaths).futureValue
                result mustBe false

                verify(mockCacheConnector).handleAmendmentErrors(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }
      }

      "when 015 rejection" - {
        "must return true" - {
          "when xPaths non-empty and errors handled" in {
            forAll(arbitrary[LocalReferenceNumber], listWithMaxLength[String]()) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.handleErrors(any(), any())(any()))
                  .thenReturn(Future.successful(true))

                val result = service.handleErrors(DeclarationRejection, lrn.value, xPaths).futureValue
                result mustBe true

                verify(mockCacheConnector).handleErrors(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }

        "must return false" - {
          "when xPaths empty" in {
            forAll(arbitrary[LocalReferenceNumber]) {
              lrn =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                val result = service.handleErrors(DeclarationRejection, lrn.value, Nil).futureValue
                result mustBe false

                verifyNoInteractions(mockCacheConnector)
            }
          }

          "when xPaths non-empty and errors not handled" in {
            forAll(arbitrary[LocalReferenceNumber], listWithMaxLength[String]()) {
              (lrn, xPaths) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                when(mockCacheConnector.handleErrors(any(), any())(any()))
                  .thenReturn(Future.successful(false))

                val result = service.handleErrors(DeclarationRejection, lrn.value, xPaths).futureValue
                result mustBe false

                verify(mockCacheConnector).handleErrors(eqTo(lrn.value), eqTo(xPaths))(any())
            }
          }
        }
      }
    }

    "nextPage" - {
      "must return correct URL" - {
        "when 013 rejection" in {
          forAll(arbitrary[LocalReferenceNumber], nonEmptyString, Gen.option(nonEmptyString)) {
            (lrn, departureId, mrn) =>
              beforeEach()

              val service = app.injector.instanceOf[BusinessRejectionTypeService]

              val result = service.nextPage(AmendmentRejection, lrn.value, departureId, mrn)

              result mustBe s"http://localhost:10120/manage-transit-movements/departures/$lrn/amend/$departureId"
          }
        }

        "when 015 rejection" - {
          "and MRN is defined" in {
            forAll(arbitrary[LocalReferenceNumber], nonEmptyString, nonEmptyString) {
              (lrn, departureId, mrn) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                val result = service.nextPage(DeclarationRejection, lrn.value, departureId, Some(mrn))

                result mustBe s"http://localhost:10120/manage-transit-movements/departures/$lrn/new-local-reference-number"
            }
          }

          "and MRN is undefined" in {
            forAll(arbitrary[LocalReferenceNumber], nonEmptyString) {
              (lrn, departureId) =>
                beforeEach()

                val service = app.injector.instanceOf[BusinessRejectionTypeService]

                val result = service.nextPage(DeclarationRejection, lrn.value, departureId, None)

                result mustBe s"http://localhost:10120/manage-transit-movements/departures/$lrn/declaration-summary"
            }
          }
        }
      }
    }
  }
}
