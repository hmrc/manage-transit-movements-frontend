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

import cats.implicits._
import connectors.ArrivalMovementP5Connector
import models.arrivalP5.{ArrivalMovementAndMessage, ArrivalMovements}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalP5MessageService @Inject() (arrivalMovementP5Connector: ArrivalMovementP5Connector) {

  def getMessagesForAllMovements(arrivalMovements: ArrivalMovements)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ArrivalMovementAndMessage]] =
    arrivalMovements.movements.traverse {
      movement =>
        arrivalMovementP5Connector
          .getMessagesForMovement(movement.messagesLocation)
          .map(
            messagesForMovement => ArrivalMovementAndMessage(movement, messagesForMovement)
          )
    }

}
