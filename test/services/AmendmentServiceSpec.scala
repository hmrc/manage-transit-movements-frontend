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
import models.departureP5.BusinessRejectionType.*
import models.departureP5.Rejection
import models.departureP5.Rejection.IE055Rejection
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class AmendmentServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockCacheConnector = mock[DepartureCacheConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheConnector))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheConnector)
  }

  "AmendmentService" - {

    "isRejectionAmendable" - {

      val rejection = IE055Rejection(departureIdP5)

      "must return true" - {
        "when rejection is amendable" in {
          forAll(arbitrary[LocalReferenceNumber]) {
            lrn =>
              beforeEach()

              val service = app.injector.instanceOf[AmendmentService]

              when(mockCacheConnector.isRejectionAmendable(any(), any())(any(), any()))
                .thenReturn(Future.successful(true))

              val result = service.isRejectionAmendable(lrn.value, rejection).futureValue
              result mustEqual true

              verify(mockCacheConnector).isRejectionAmendable(eqTo(lrn.value), eqTo(rejection))(any(), any())
          }
        }
      }

      "must return false" - {
        "when rejection is not amendable" in {
          forAll(arbitrary[LocalReferenceNumber]) {
            lrn =>
              beforeEach()

              val service = app.injector.instanceOf[AmendmentService]

              when(mockCacheConnector.isRejectionAmendable(any(), any())(any(), any()))
                .thenReturn(Future.successful(false))

              val result = service.isRejectionAmendable(lrn.value, rejection).futureValue
              result mustEqual false

              verify(mockCacheConnector).isRejectionAmendable(eqTo(lrn.value), eqTo(rejection))(any(), any())
          }
        }
      }
    }

    "handleErrors" - {
      "must handle errors" in {
        forAll(arbitrary[LocalReferenceNumber]) {
          lrn =>
            beforeEach()

            val service = app.injector.instanceOf[AmendmentService]

            when(mockCacheConnector.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(OK)))

            val rejection = IE055Rejection(departureIdP5)

            val result = service.handleErrors(lrn.value, rejection).futureValue
            result.status mustEqual OK

            verify(mockCacheConnector).handleErrors(eqTo(lrn.value), eqTo(rejection))(any(), any())
        }
      }
    }

    "prepareForAmendment" - {
      "must prepare user answers for amendment" in {
        forAll(arbitrary[LocalReferenceNumber]) {
          lrn =>
            beforeEach()

            val service = app.injector.instanceOf[AmendmentService]

            when(mockCacheConnector.prepareForAmendment(any(), any())(any()))
              .thenReturn(Future.successful(httpResponse(OK)))

            val result = service.prepareForAmendment(lrn.value, departureIdP5).futureValue
            result.status mustEqual OK

            verify(mockCacheConnector).prepareForAmendment(eqTo(lrn.value), eqTo(departureIdP5))(any())
        }
      }
    }

    "nextPage" - {
      "must return correct URL" - {
        "when 013 rejection" in {
          forAll(arbitrary[LocalReferenceNumber], Gen.option(nonEmptyString)) {
            (lrn, mrn) =>
              beforeEach()

              val service = app.injector.instanceOf[AmendmentService]

              val result = service.nextPage(AmendmentRejection, lrn.value, mrn)

              result mustEqual s"http://localhost:10120/manage-transit-movements/departures/$lrn/declaration-summary"
          }
        }

        "when 015 rejection" - {
          "and MRN is defined" in {
            forAll(arbitrary[LocalReferenceNumber], nonEmptyString) {
              (lrn, mrn) =>
                beforeEach()

                val service = app.injector.instanceOf[AmendmentService]

                val result = service.nextPage(DeclarationRejection, lrn.value, Some(mrn))

                result mustEqual s"http://localhost:10120/manage-transit-movements/departures/$lrn/new-local-reference-number"
            }
          }

          "and MRN is undefined" in {
            forAll(arbitrary[LocalReferenceNumber]) {
              lrn =>
                beforeEach()

                val service = app.injector.instanceOf[AmendmentService]

                val result = service.nextPage(DeclarationRejection, lrn.value, None)

                result mustEqual s"http://localhost:10120/manage-transit-movements/departures/$lrn/declaration-summary"
            }
          }
        }
      }
    }
  }

}
