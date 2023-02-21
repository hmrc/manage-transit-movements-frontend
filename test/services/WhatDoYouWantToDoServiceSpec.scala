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
import models.Availability
import org.mockito.Mockito.{reset, when}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global


class WhatDoYouWantToDoServiceSpec extends SpecBase with Generators {

  val mockArrivalConnector: ArrivalMovementConnector                     = mock[ArrivalMovementConnector]
  val mockDeparturesMovementConnector: DeparturesMovementConnector       = mock[DeparturesMovementConnector]
  val mockDeparturesMovementsP5Connector: DeparturesMovementsP5Connector = mock[DeparturesMovementsP5Connector]
  val mockArrivalMovementP5Connector: ArrivalMovementP5Connector         = mock[ArrivalMovementP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalConnector)
    reset(mockDeparturesMovementConnector)
    reset(mockDeparturesMovementsP5Connector)
    reset(mockArrivalMovementP5Connector)
  }

  val whatDoYouWantToDoService = new WhatDoYouWantToDoService(mockArrivalConnector,
                                                              mockDeparturesMovementConnector,
                                                              mockDeparturesMovementsP5Connector,
                                                              mockArrivalMovementP5Connector,
                                                              frontendAppConfig
  )

  "WhatDoYouWantToDoService" - {
    "fetchArrivalsAvailability" - {
      "must getAllMovements when phase5ArrivalEnabled" in {
        when(frontendAppConfig.phase5ArrivalEnabled)
        when(mockArrivalMovementP5Connector.getAllMovements(Future.successful(Availability.NonEmpty)))
        whatDoYouWantToDoService.fetchArrivalsAvailability().futureValue mustBe Availability.NonEmpty
      }
    }

  }

}
