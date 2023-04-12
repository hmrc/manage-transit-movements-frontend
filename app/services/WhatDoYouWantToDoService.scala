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

import connectors.{ArrivalMovementConnector, ArrivalMovementP5Connector, DeparturesDraftsP5Connector, DeparturesMovementConnector}
import models.{Availability, DraftAvailability}
import uk.gov.hmrc.http.HeaderCarrier
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoService @Inject() (arrivalMovementConnector: ArrivalMovementConnector,
                                          departuresMovementConnector: DeparturesMovementConnector,
                                          departuresMovementsP5Connector: DeparturesDraftsP5Connector,
                                          arrivalMovementsP5Connector: ArrivalMovementP5Connector
) {

  def fetchArrivalsAvailability(phase5ArrivalEnabled: Boolean)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Availability] =
    if (phase5ArrivalEnabled) {
      arrivalMovementsP5Connector.getAllMovements().map(Availability(_)) //TODO update when we have API params
    } else {
      arrivalMovementConnector.getArrivalsAvailability()
    }

  def fetchArrivalsUrl(phase5ArrivalEnabled: Boolean): String =
    if (phase5ArrivalEnabled) {
      controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url
    } else {
      controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
    }

  def getDeparturesAvailability(implicit hc: HeaderCarrier): Future[Availability] =
    departuresMovementConnector.getDeparturesAvailability()

  def fetchDraftDepartureAvailability(phase5DepartureEnabled: Boolean)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DraftAvailability]] =
    if (phase5DepartureEnabled) {
      departuresMovementsP5Connector.getDraftDeparturesAvailability().map(Some(_))
    } else {
      Future.successful(None)
    }
}
