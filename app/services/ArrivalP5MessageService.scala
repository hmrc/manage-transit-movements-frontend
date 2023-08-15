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

import cats.data.OptionT
import cats.implicits._
import connectors.ArrivalMovementP5Connector
import models.arrivalP5.ArrivalMessageType.{ArrivalNotification, RejectionFromOfficeOfDestination}
import models.arrivalP5.{ArrivalMessageMetaData, ArrivalMessageType, ArrivalMovementAndMessage, ArrivalMovements, IE057Data}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpReads.Implicits._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalP5MessageService @Inject() (arrivalMovementP5Connector: ArrivalMovementP5Connector) {

  def getMessagesForAllMovements(arrivalMovements: ArrivalMovements)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ArrivalMovementAndMessage]] =
    arrivalMovements.arrivalMovements.traverse {
      movement =>
        for {
          messagesForMovement <- arrivalMovementP5Connector.getMessagesForMovement(movement.messagesLocation)
          ie057               <- getMessage[IE057Data](movement.arrivalId, RejectionFromOfficeOfDestination)
          functionalErrorsCount = ie057.map(_.data.functionalErrors.length).getOrElse(0)
        } yield ArrivalMovementAndMessage(movement, messagesForMovement, functionalErrorsCount)
    }

  def getMessage[MessageModel](
    arrivalId: String,
    typeOfMessage: ArrivalMessageType
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, httpReads: HttpReads[MessageModel]): Future[Option[MessageModel]] =
    (
      for {
        messageMetaData <- OptionT(getSpecificMessageMetaData(arrivalId, typeOfMessage))
        message         <- OptionT.liftF(arrivalMovementP5Connector.getSpecificMessage[MessageModel](messageMetaData.path))
      } yield message
    ).value

  def getMessageWithMessageId[MessageModel](
    arrivalId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, httpReads: HttpReads[MessageModel]): Future[Option[MessageModel]] =
    (
      for {
        messageMetaData <- OptionT(getMessageMetaDataWithMessageId(arrivalId, messageId))
        message         <- OptionT.liftF(arrivalMovementP5Connector.getSpecificMessage[MessageModel](messageMetaData.path))
      } yield message
    ).value

  private def getSpecificMessageMetaData[T <: ArrivalMessageType](arrivalId: String, typeOfMessage: T)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[ArrivalMessageMetaData]] =
    getMessageMetaData(arrivalId, typeOfMessage)

  private def getMessageMetaData(arrivalId: String, messageType: ArrivalMessageType)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[ArrivalMessageMetaData]] =
    arrivalMovementP5Connector
      .getMessageMetaData(arrivalId)
      .map(
        _.messages
          .filter(_.messageType == messageType)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  private def getMessageMetaDataWithMessageId(arrivalId: String, messageId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[ArrivalMessageMetaData]] =
    arrivalMovementP5Connector
      .getMessageMetaDataForMessageId(arrivalId, messageId)

}
