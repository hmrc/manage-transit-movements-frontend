/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.{DestinationConnector, ReferenceDataConnector}
import javax.inject.Inject
import models.referenceData.Movement
import play.api.mvc.MessagesControllerComponents
import renderer.Renderer
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.ViewMovement

import scala.concurrent.{ExecutionContext, Future}

class CustomOfficeLookupService @Inject()(referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def convertToViewMovements(movement: Movement)(implicit hc: HeaderCarrier): Future[ViewMovement] =
    referenceDataConnector.getCustomsOffice(movement.presentationOfficeId) map {
      presentationOffice =>
        ViewMovement(
          movement.date,
          movement.time,
          movement.movementReferenceNumber,
          movement.traderName,
          movement.presentationOfficeId,
          presentationOffice.flatMap(_.asOpt).map(_.name), // TODO: Alerting - We are dropping this json parse failure
          movement.procedure
        )
    }

}
