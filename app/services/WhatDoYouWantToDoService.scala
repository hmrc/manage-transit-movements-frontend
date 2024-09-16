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

import connectors._
import models.Feature
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoService @Inject() (
  departuresMovementP5Connector: DepartureMovementP5Connector,
  departureDraftsP5Connector: DeparturesDraftsP5Connector,
  arrivalMovementsP5Connector: ArrivalMovementP5Connector
) {

  def fetchArrivalsFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      availability <- arrivalMovementsP5Connector.getAvailability()
    } yield Feature(availability, controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url)

  def fetchDeparturesFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      availability <- departuresMovementP5Connector.getAvailability()
    } yield Feature(availability, controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url)

  def fetchDraftDepartureFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      draftsAvailability <- departureDraftsP5Connector.getDraftDeparturesAvailability()
    } yield Feature(draftsAvailability, controllers.departureP5.drafts.routes.DashboardController.onPageLoad(None, None, None).url)
}
