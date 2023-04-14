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
import connectors._
import generators.Generators
import models.arrivalP5.{ArrivalMovement, ArrivalMovements}
import models.{Availability, DraftAvailability}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verifyNoInteractions, when}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatDoYouWantToDoServiceSpec extends SpecBase with Generators {

  val mockArrivalConnector: ArrivalMovementConnector                  = mock[ArrivalMovementConnector]
  val mockDeparturesMovementConnector: DeparturesMovementConnector    = mock[DeparturesMovementConnector]
  val mockDeparturesMovementsP5Connector: DeparturesDraftsP5Connector = mock[DeparturesDraftsP5Connector]
  val mockArrivalMovementP5Connector: ArrivalMovementP5Connector      = mock[ArrivalMovementP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalConnector)
    reset(mockDeparturesMovementConnector)
    reset(mockDeparturesMovementsP5Connector)
    reset(mockArrivalMovementP5Connector)
  }

  val whatDoYouWantToDoService =
    new WhatDoYouWantToDoService(mockArrivalConnector, mockDeparturesMovementConnector, mockDeparturesMovementsP5Connector, mockArrivalMovementP5Connector)

  "WhatDoYouWantToDoService" - {

    "fetchArrivalsAvailability" - {
      "must get availability when phase 5 enabled" in {

        val expectedResult = ArrivalMovements(
          Seq(
            ArrivalMovement(
              "63651574c3447b12",
              "27WF9X1FQ9RCKN0TM3",
              LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
              "movements/arrivals/63651574c3447b12/messages"
            )
          )
        )

        when(mockArrivalMovementP5Connector.getAllMovements()(any())).thenReturn(Future.successful(Some(expectedResult)))
        whatDoYouWantToDoService.fetchArrivalsAvailability(phase5ArrivalEnabled = true).futureValue mustBe Availability.NonEmpty

        verifyNoInteractions(mockDeparturesMovementConnector)
      }

      "must get availability when phase 5 disabled" in {

        when(mockArrivalConnector.getArrivalsAvailability()(any())).thenReturn(Future.successful(Availability.NonEmpty))
        whatDoYouWantToDoService.fetchArrivalsAvailability(phase5ArrivalEnabled = false).futureValue mustBe Availability.NonEmpty

        verifyNoInteractions(mockArrivalMovementP5Connector)
      }
    }

    "getDeparturesAvailability" - {

      "must get availability" in {

        when(mockDeparturesMovementConnector.getDeparturesAvailability()(any())).thenReturn(Future.successful(Availability.NonEmpty))
        whatDoYouWantToDoService.getDeparturesAvailability.futureValue mustBe Availability.NonEmpty

        verifyNoInteractions(mockDeparturesMovementsP5Connector)
      }
    }

    "fetchDraftArrivalsAvailability" - {
      "must get availability when phase 5 enabled" in {

        when(mockDeparturesMovementsP5Connector.getDraftDeparturesAvailability()(any())).thenReturn(Future.successful(DraftAvailability.NonEmpty))
        whatDoYouWantToDoService.fetchDraftDepartureAvailability(phase5DepartureEnabled = true).futureValue.value mustBe DraftAvailability.NonEmpty

        verifyNoInteractions(mockDeparturesMovementConnector)
      }

      "must return None when phase 5 disabled" in {

        whatDoYouWantToDoService.fetchDraftDepartureAvailability(phase5DepartureEnabled = false).futureValue mustBe None

        verifyNoInteractions(mockDeparturesMovementsP5Connector)
        verifyNoInteractions(mockDeparturesMovementConnector)
      }
    }

    "fetchArrivalsUrl" - {
      "must get correct URL when phase 5 enabled" in {

        whatDoYouWantToDoService.fetchArrivalsUrl(phase5ArrivalEnabled = true) mustBe
          controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad().url
      }

      "must get correct URL when phase 5 disabled" in {

        whatDoYouWantToDoService.fetchArrivalsUrl(phase5ArrivalEnabled = false) mustBe
          controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
      }
    }

  }

}
