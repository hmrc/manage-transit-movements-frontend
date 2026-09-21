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

import connectors.*
import models.Feature
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoService @Inject() (
  departuresMovementConnector: DepartureMovementConnector,
  departureDraftsConnector: DeparturesDraftsConnector,
  arrivalMovementsConnector: ArrivalMovementConnector
) {

  def fetchArrivalsFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      availability <- arrivalMovementsConnector.getAvailability()
    } yield Feature(availability, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None, None).url)

  def fetchDeparturesFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      availability <- departuresMovementConnector.getAvailability()
    } yield Feature(availability, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None, None).url)

  def fetchDraftDepartureFeature()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Feature] =
    for {
      draftsAvailability <- departureDraftsConnector.getDraftDeparturesAvailability()
    } yield Feature(draftsAvailability, controllers.departure.drafts.routes.DashboardController.onPageLoad(None, None).url)

}
