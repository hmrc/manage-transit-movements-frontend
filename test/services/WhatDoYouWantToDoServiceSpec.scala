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
import config.FrontendAppConfig
import connectors._
import generators.Generators
import models.Availability
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatDoYouWantToDoServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockArrivalConnector: ArrivalMovementConnector                   = mock[ArrivalMovementConnector]
  val mockDeparturesMovementConnector: DeparturesMovementConnector     = mock[DeparturesMovementConnector]
  val mockDeparturesDraftsP5Connector: DeparturesDraftsP5Connector     = mock[DeparturesDraftsP5Connector]
  val mockDeparturesMovementsP5Connector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]
  val mockArrivalMovementP5Connector: ArrivalMovementP5Connector       = mock[ArrivalMovementP5Connector]

  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalConnector)
    reset(mockDeparturesMovementConnector)
    reset(mockDeparturesMovementsP5Connector)
    reset(mockDeparturesDraftsP5Connector)
    reset(mockArrivalMovementP5Connector)
  }

  val whatDoYouWantToDoService =
    new WhatDoYouWantToDoService(
      mockFrontendAppConfig,
      mockArrivalConnector,
      mockDeparturesMovementConnector,
      mockDeparturesMovementsP5Connector,
      mockDeparturesDraftsP5Connector,
      mockArrivalMovementP5Connector
    )

  "WhatDoYouWantToDoService" - {

    "fetchArrivalsAvailability" - {
      "must get availability when phase 5 enabled" in {
        forAll(arbitrary[Availability]) {
          availability =>
            when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)
            when(mockArrivalMovementP5Connector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe availability

            verifyNoInteractions(mockArrivalConnector)
        }
      }

      "must get availability when phase 5 disabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)
        when(mockArrivalConnector.getArrivalsAvailability()(any())).thenReturn(Future.successful(Availability.NonEmpty))

        whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe Availability.NonEmpty

        verifyNoInteractions(mockArrivalMovementP5Connector)
      }
    }

    "fetchDeparturesAvailability" - {
      "must get availability when phase 5 enabled" in {
        forAll(arbitrary[Availability]) {
          availability =>
            when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)
            when(mockDeparturesMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe availability

            verifyNoInteractions(mockDeparturesMovementConnector)
        }
      }

      "must get availability when phase 5 disabled" in {
        forAll(arbitrary[Availability]) {
          availability =>
            when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)
            when(mockDeparturesMovementConnector.getDeparturesAvailability()(any())).thenReturn(Future.successful(availability))

            whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe availability

            verifyNoInteractions(mockArrivalMovementP5Connector)
        }
      }
    }

    "fetchDraftDeparturesAvailability" - {
      "must get availability when phase 5 enabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)
        when(mockDeparturesDraftsP5Connector.getDraftDeparturesAvailability()(any())).thenReturn(Future.successful(Availability.NonEmpty))

        whatDoYouWantToDoService.fetchDraftDepartureAvailability().futureValue.value mustBe Availability.NonEmpty

        verifyNoInteractions(mockDeparturesMovementConnector)
      }

      "must return None when phase 5 disabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

        whatDoYouWantToDoService.fetchDraftDepartureAvailability().futureValue mustBe None

        verifyNoInteractions(mockDeparturesDraftsP5Connector)
        verifyNoInteractions(mockDeparturesMovementConnector)
      }
    }

    "fetchArrivalsUrl" - {
      "must get correct URL when phase 5 enabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

        whatDoYouWantToDoService.fetchArrivalsUrl() mustBe
          controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
      }

      "must get correct URL when phase 5 disabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

        whatDoYouWantToDoService.fetchArrivalsUrl() mustBe
          controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
      }
    }

    "fetchDeparturesUrl" - {
      "must get correct URL when phase 5 enabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

        whatDoYouWantToDoService.fetchDeparturesUrl() mustBe
          controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
      }

      "must get correct URL when phase 5 disabled" in {
        when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

        whatDoYouWantToDoService.fetchDeparturesUrl() mustBe
          controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url
      }
    }

  }

}
