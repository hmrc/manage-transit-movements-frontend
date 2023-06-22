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

import config.FrontendAppConfig
import connectors._
import models.{Availability, DraftAvailability}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoService @Inject() (
  appConfig: FrontendAppConfig,
  arrivalMovementConnector: ArrivalMovementConnector,
  departuresMovementConnector: DeparturesMovementConnector,
  departuresMovementP5Connector: DepartureMovementP5Connector,
  departureDraftsP5Connector: DeparturesDraftsP5Connector,
  arrivalMovementsP5Connector: ArrivalMovementP5Connector
) {

  def fetchArrivalsAvailability()(implicit hc: HeaderCarrier): Future[Availability] =
    if (appConfig.phase5ArrivalEnabled) {
      arrivalMovementsP5Connector.getAvailability()
    } else {
      arrivalMovementConnector.getArrivalsAvailability()
    }

  def fetchArrivalsUrl(): String =
    if (appConfig.phase5ArrivalEnabled) {
      controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url
    } else {
      controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
    }

  def fetchDeparturesAvailability()(implicit hc: HeaderCarrier): Future[Availability] =
    if (appConfig.phase5DepartureEnabled) {
      departuresMovementP5Connector.getAvailability()
    } else {
      departuresMovementConnector.getDeparturesAvailability()
    }

  def fetchDeparturesUrl(): String =
    if (appConfig.phase5DepartureEnabled) {
      controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
    } else {
      controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url
    }

  def fetchDraftDepartureAvailability()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DraftAvailability]] =
    if (appConfig.phase5DepartureEnabled) {
      departureDraftsP5Connector.getDraftDeparturesAvailability().map(Some(_))
    } else {
      Future.successful(None)
    }
}
