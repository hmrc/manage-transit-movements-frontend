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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.*
import generators.Generators
import models.{Availability, Feature}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatDoYouWantToDoServiceSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  val mockArrivalMovementsP5Connector: ArrivalMovementP5Connector = mock[ArrivalMovementP5Connector]

  val mockDepartureMovementsP5Connector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]
  val mockDepartureDraftsP5Connector: DeparturesDraftsP5Connector     = mock[DeparturesDraftsP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMovementsP5Connector)
    reset(mockDepartureMovementsP5Connector)
    reset(mockDepartureDraftsP5Connector)
  }

  val whatDoYouWantToDoService =
    new WhatDoYouWantToDoService(
      mockDepartureMovementsP5Connector,
      mockDepartureDraftsP5Connector,
      mockArrivalMovementsP5Connector
    )

  "WhatDoYouWantToDoService" - {

    "fetchArrivalsAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockArrivalMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchArrivalsFeature().futureValue mustEqual
              Feature(availability, controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url)

            verify(mockArrivalMovementsP5Connector).getAvailability()
        }
      }
    }

    "fetchDeparturesAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockDepartureMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDeparturesFeature().futureValue mustEqual
              Feature(availability, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url)

            verify(mockDepartureMovementsP5Connector).getAvailability()
        }
      }
    }

    "fetchDraftDeparturesAvailability" - {
      "must get availability" in {
        forAll(arbitrary[Availability]) {
          availability =>
            beforeEach()

            when(mockDepartureDraftsP5Connector.getDraftDeparturesAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDraftDepartureFeature().futureValue mustEqual
              Feature(availability, controllers.departureP5.drafts.routes.DashboardController.onPageLoad(None, None).url)

            verify(mockDepartureDraftsP5Connector).getDraftDeparturesAvailability()(any())
        }
      }
    }
  }

}
