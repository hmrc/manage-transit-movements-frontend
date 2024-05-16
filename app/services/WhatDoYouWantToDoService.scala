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
import models.{Feature, Features}
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

  def fetchArrivalsAvailability()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Features] =
    (appConfig.phase4Enabled, appConfig.phase5Enabled) match {
      case (false, false) =>
        Future.successful(Features(None, None))
      case (true, false) =>
        for {
          p4Availability <- arrivalMovementConnector.getArrivalsAvailability()
        } yield Features(
          Some(Feature(p4Availability, enabled = true, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)),
          None
        )
      case (isPhase4Enabled, true) =>
        for {
          p4Availability <- arrivalMovementConnector.getArrivalsAvailability()
          p5Availability <- arrivalMovementsP5Connector.getAvailability()
        } yield Features(
          Some(Feature(p4Availability, isPhase4Enabled, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)),
          Some(Feature(p5Availability, enabled = true, controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url))
        )
    }

  def fetchDeparturesAvailability()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Features] =
    (appConfig.phase4Enabled, appConfig.phase5Enabled) match {
      case (false, false) =>
        Future.successful(Features(None, None))
      case (true, false) =>
        for {
          p4Availability <- departuresMovementConnector.getDeparturesAvailability()
        } yield Features(
          Some(Feature(p4Availability, enabled = true, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)),
          None
        )
      case (isPhaseEnabled, true) =>
        for {
          p4Availability <- departuresMovementConnector.getDeparturesAvailability()
          p5Availability <- departuresMovementP5Connector.getAvailability()
        } yield Features(
          Some(Feature(p4Availability, isPhaseEnabled, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)),
          Some(Feature(p5Availability, enabled = true, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url))
        )
    }

  def fetchDraftDepartureAvailability()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Features] =
    if (appConfig.phase5Enabled) {
      for {
        draftsAvailability <- departureDraftsP5Connector.getDraftDeparturesAvailability()
      } yield Features(
        None,
        Some(Feature(draftsAvailability, enabled = true, controllers.departureP5.drafts.routes.DashboardController.onPageLoad(None, None, None).url))
      )
    } else {
      Future.successful(Features(None, None))
    }
}
