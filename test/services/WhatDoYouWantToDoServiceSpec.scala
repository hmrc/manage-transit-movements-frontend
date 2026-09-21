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

package services

import base.SpecBase
import connectors.*
import generators.Generators
import models.{Availability, Feature}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatDoYouWantToDoServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  val mockArrivalMovementsConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  val mockDepartureMovementsConnector: DepartureMovementConnector = mock[DepartureMovementConnector]
  val mockDepartureDraftsConnector: DeparturesDraftsConnector     = mock[DeparturesDraftsConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMovementsConnector)
    reset(mockDepartureMovementsConnector)
    reset(mockDepartureDraftsConnector)
  }

  val whatDoYouWantToDoService =
    new WhatDoYouWantToDoService(
      mockDepartureMovementsConnector,
      mockDepartureDraftsConnector,
      mockArrivalMovementsConnector
    )

  "WhatDoYouWantToDoService" - {

    "fetchArrivalsAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockArrivalMovementsConnector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchArrivalsFeature().futureValue mustEqual
              Feature(availability, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None, None).url)

            verify(mockArrivalMovementsConnector).getAvailability()
        }
      }
    }

    "fetchDeparturesAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockDepartureMovementsConnector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDeparturesFeature().futureValue mustEqual
              Feature(availability, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None, None).url)

            verify(mockDepartureMovementsConnector).getAvailability()
        }
      }
    }

    "fetchDraftDeparturesAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockDepartureDraftsConnector.getDraftDeparturesAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDraftDepartureFeature().futureValue mustEqual
              Feature(availability, controllers.departure.drafts.routes.DashboardController.onPageLoad(None, None).url)

            verify(mockDepartureDraftsConnector).getDraftDeparturesAvailability()(any())
        }
      }
    }
  }

}
