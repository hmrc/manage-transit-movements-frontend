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
import generated.{CC025CType, CC057CType}
import models.arrivalP5.ArrivalMessageType._
import models.arrivalP5._
import scalaxb.XMLFormat
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import generated.Generated_CC025CTypeFormat
import generated.Generated_CC057CTypeFormat

class ArrivalP5MessageService @Inject() (arrivalMovementP5Connector: ArrivalMovementP5Connector) {

  def getLatestMessagesForMovements(
    arrivalMovements: ArrivalMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ArrivalMovementAndMessage]] =
    arrivalMovements.arrivalMovements.traverse {
      movement =>
        arrivalMovementP5Connector.getLatestMessageForMovement(movement.arrivalId).flatMap {
          message =>
            message.latestMessage.messageType match {
              case GoodsReleasedNotification =>
                getMessage[CC025CType](movement.arrivalId, message.latestMessage.messageId).map {
                  ie025Data =>
                    GoodsReleasedMovementAndMessage(
                      movement,
                      message,
                      ie025Data.TransitOperation.releaseIndicator
                    )
                }
              case RejectionFromOfficeOfDestination =>
                getMessage[CC057CType](movement.arrivalId, message.latestMessage.messageId).map {
                  ie057Data =>
                    val functionalErrorCount  = ie057Data.FunctionalError.length
                    val businessRejectionType = ie057Data.TransitOperation.businessRejectionType

                    RejectedMovementAndMessage(movement, message, functionalErrorCount, businessRejectionType)
                }
              case _ => Future.successful(OtherMovementAndMessage(movement, message))
            }

        }
    }

  def getMessage[T](
    arrivalId: String,
    messageId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, format: XMLFormat[T]): Future[T] =
    arrivalMovementP5Connector.getMessage(arrivalId, messageId)

}
