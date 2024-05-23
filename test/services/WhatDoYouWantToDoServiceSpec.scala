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
import models.{Availability, Feature, Features}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatDoYouWantToDoServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockArrivalMovementsConnector: ArrivalMovementConnector     = mock[ArrivalMovementConnector]
  val mockArrivalMovementsP5Connector: ArrivalMovementP5Connector = mock[ArrivalMovementP5Connector]

  val mockDepartureMovementsConnector: DeparturesMovementConnector    = mock[DeparturesMovementConnector]
  val mockDepartureMovementsP5Connector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]
  val mockDepartureDraftsP5Connector: DeparturesDraftsP5Connector     = mock[DeparturesDraftsP5Connector]

  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMovementsConnector)
    reset(mockArrivalMovementsP5Connector)
    reset(mockDepartureMovementsConnector)
    reset(mockDepartureMovementsP5Connector)
    reset(mockDepartureDraftsP5Connector)
  }

  val whatDoYouWantToDoService =
    new WhatDoYouWantToDoService(
      mockFrontendAppConfig,
      mockArrivalMovementsConnector,
      mockDepartureMovementsConnector,
      mockDepartureMovementsP5Connector,
      mockDepartureDraftsP5Connector,
      mockArrivalMovementsP5Connector
    )

  "WhatDoYouWantToDoService" - {

    "fetchArrivalsAvailability" - {
      "must get availability" - {
        "when phase 4 disabled and phase 5 disabled" in {
          when(mockFrontendAppConfig.phase4Enabled).thenReturn(false)
          when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

          whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe
            Features(
              phase4 = None,
              phase5 = None
            )

          verifyNoInteractions(mockArrivalMovementsConnector)
          verifyNoInteractions(mockArrivalMovementsP5Connector)
        }

        "when phase 4 enabled and phase 5 disabled" in {
          forAll(arbitrary[Availability]) {
            availability =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(true)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

              when(mockArrivalMovementsConnector.getArrivalsAvailability()(any())).thenReturn(Future.successful(availability))

              whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(availability, enabled = true, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)),
                  phase5 = None
                )

              verify(mockArrivalMovementsConnector).getArrivalsAvailability()
              verifyNoInteractions(mockArrivalMovementsP5Connector)
          }
        }

        "when phase 4 disabled and phase 5 enabled" in {
          forAll(arbitrary[Availability], arbitrary[Availability]) {
            (p4Availability, p5Availability) =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(false)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

              when(mockArrivalMovementsConnector.getArrivalsAvailability()(any())).thenReturn(Future.successful(p4Availability))
              when(mockArrivalMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(p5Availability))

              whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(p4Availability, enabled = false, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)),
                  phase5 = Some(Feature(p5Availability, enabled = true, controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url))
                )

              verify(mockArrivalMovementsConnector).getArrivalsAvailability()
              verify(mockArrivalMovementsP5Connector).getAvailability()
          }
        }

        "when phase 4 enabled and phase 5 enabled" in {
          forAll(arbitrary[Availability], arbitrary[Availability]) {
            (p4Availability, p5Availability) =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(true)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

              when(mockArrivalMovementsConnector.getArrivalsAvailability()(any())).thenReturn(Future.successful(p4Availability))
              when(mockArrivalMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(p5Availability))

              whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(p4Availability, enabled = true, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)),
                  phase5 = Some(Feature(p5Availability, enabled = true, controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url))
                )

              verify(mockArrivalMovementsConnector).getArrivalsAvailability()
              verify(mockArrivalMovementsP5Connector).getAvailability()
          }
        }
      }
    }

    "fetchDeparturesAvailability" - {
      "must get availability" - {
        "when phase 4 disabled and phase 5 disabled" in {
          when(mockFrontendAppConfig.phase4Enabled).thenReturn(false)
          when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

          whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe
            Features(
              phase4 = None,
              phase5 = None
            )

          verifyNoInteractions(mockDepartureMovementsConnector)
          verifyNoInteractions(mockDepartureMovementsP5Connector)
        }

        "when phase 4 enabled and phase 5 disabled" in {
          forAll(arbitrary[Availability]) {
            availability =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(true)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

              when(mockDepartureMovementsConnector.getDeparturesAvailability()(any())).thenReturn(Future.successful(availability))

              whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(availability, enabled = true, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)),
                  phase5 = None
                )

              verify(mockDepartureMovementsConnector).getDeparturesAvailability()
              verifyNoInteractions(mockDepartureMovementsP5Connector)
          }
        }

        "when phase 4 disabled and phase 5 enabled" in {
          forAll(arbitrary[Availability], arbitrary[Availability]) {
            (p4Availability, p5Availability) =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(false)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

              when(mockDepartureMovementsConnector.getDeparturesAvailability()(any())).thenReturn(Future.successful(p4Availability))
              when(mockDepartureMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(p5Availability))

              whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(p4Availability, enabled = false, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)),
                  phase5 =
                    Some(Feature(p5Availability, enabled = true, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url))
                )

              verify(mockDepartureMovementsConnector).getDeparturesAvailability()
              verify(mockDepartureMovementsP5Connector).getAvailability()
          }
        }

        "when phase 4 enabled and phase 5 enabled" in {
          forAll(arbitrary[Availability], arbitrary[Availability]) {
            (p4Availability, p5Availability) =>
              beforeEach()

              when(mockFrontendAppConfig.phase4Enabled).thenReturn(true)
              when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

              when(mockDepartureMovementsConnector.getDeparturesAvailability()(any())).thenReturn(Future.successful(p4Availability))
              when(mockDepartureMovementsP5Connector.getAvailability()(any())).thenReturn(Future.successful(p5Availability))

              whatDoYouWantToDoService.fetchDeparturesAvailability().futureValue mustBe
                Features(
                  phase4 = Some(Feature(p4Availability, enabled = true, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)),
                  phase5 =
                    Some(Feature(p5Availability, enabled = true, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url))
                )

              verify(mockDepartureMovementsConnector).getDeparturesAvailability()
              verify(mockDepartureMovementsP5Connector).getAvailability()
          }
        }
      }
    }

    "fetchDraftDeparturesAvailability" - {
      "must get availability" - {
        "when phase 5 enabled" in {
          forAll(arbitrary[Availability]) {
            availability =>
              beforeEach()

              when(mockFrontendAppConfig.phase5Enabled).thenReturn(true)

              when(mockDepartureDraftsP5Connector.getDraftDeparturesAvailability()(any())).thenReturn(Future.successful(availability))

              whatDoYouWantToDoService.fetchDraftDepartureAvailability().futureValue mustBe
                Features(
                  None,
                  Some(Feature(availability, enabled = true, controllers.departureP5.drafts.routes.DashboardController.onPageLoad(None, None, None).url))
                )

              verifyNoInteractions(mockDepartureMovementsConnector)
          }
        }

        "when phase 5 disabled" in {
          when(mockFrontendAppConfig.phase5Enabled).thenReturn(false)

          whatDoYouWantToDoService.fetchDraftDepartureAvailability().futureValue mustBe
            Features(
              None,
              None
            )

          verifyNoInteractions(mockDepartureDraftsP5Connector)
        }
      }
    }
  }
}
