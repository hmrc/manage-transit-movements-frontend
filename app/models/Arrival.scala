/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import models.arrival.ArrivalStatus.{
  ArrivalNotificationSubmitted,
  ArrivalRejection,
  UnloadingRemarksRejection,
  UnloadingRemarksSubmitted,
  XMLSubmissionNegativeAcknowledgement
}
import models.arrival.{ArrivalMessageMetaData, ArrivalStatus}
import play.api.libs.json.{Json, Reads}

import java.time.LocalDateTime

case class Arrival(
  arrivalId: ArrivalId,
  created: LocalDateTime,
  updated: LocalDateTime,
  messagesMetaData: Seq[ArrivalMessageMetaData],
  movementReferenceNumber: String
) extends Movement {

  def currentStatus: ArrivalStatus = {

    implicit val localDateOrdering: Ordering[LocalDateTime] = _ compareTo _

    val latestMessage            = messagesMetaData.maxBy(_.dateTime)
    val messagesWithSameDateTime = messagesMetaData.filter(_.dateTime == latestMessage.dateTime)

    if (messagesWithSameDateTime.size == 1) {
      latestMessage.messageType
    } else {

      messagesWithSameDateTime.map(_.messageType).max match {
        case ArrivalRejection =>
          if (messagesMetaData.count(_.messageType == ArrivalNotificationSubmitted) > messagesMetaData.count(_.messageType == ArrivalRejection)) {
            ArrivalNotificationSubmitted
          } else {
            ArrivalRejection
          }
        case UnloadingRemarksRejection =>
          if (messagesMetaData.count(_.messageType == UnloadingRemarksSubmitted) > messagesMetaData.count(_.messageType == UnloadingRemarksRejection)) {
            UnloadingRemarksSubmitted
          } else {
            UnloadingRemarksRejection
          }
        case XMLSubmissionNegativeAcknowledgement if messagesMetaData.count(_.messageType == UnloadingRemarksSubmitted) >= 1 =>
          if (
            messagesMetaData.count(_.messageType == UnloadingRemarksSubmitted) > messagesMetaData.count(_.messageType == XMLSubmissionNegativeAcknowledgement)
          ) {
            UnloadingRemarksSubmitted
          } else {
            XMLSubmissionNegativeAcknowledgement
          }
        case XMLSubmissionNegativeAcknowledgement =>
          if (
            messagesMetaData
              .count(_.messageType == ArrivalNotificationSubmitted) > messagesMetaData.count(_.messageType == XMLSubmissionNegativeAcknowledgement)
          ) {
            ArrivalNotificationSubmitted
          } else {
            XMLSubmissionNegativeAcknowledgement
          }
        case value => value
      }
    }
  }

  def previousStatus: ArrivalStatus = {

    implicit val localDateOrdering: Ordering[LocalDateTime] = _ compareTo _

    val previousMessage = messagesMetaData.sortBy(_.dateTime).takeRight(2).head

    val messagesWithSameDateTime = messagesMetaData.filter(_.dateTime == previousMessage.dateTime)

    if (messagesWithSameDateTime.size == 1) {
      previousMessage.messageType
    } else {

      currentStatus match {
        case ArrivalStatus.XMLSubmissionNegativeAcknowledgement =>
          if (previousMessage.messageType == ArrivalNotificationSubmitted | previousMessage.messageType == UnloadingRemarksSubmitted) {
            previousMessage.messageType
          } else {
            messagesWithSameDateTime.map(_.messageType).max
          }
        case _ => messagesWithSameDateTime.map(_.messageType).max
      }
    }
  }
}

object Arrival {

  implicit val reads: Reads[Arrival] = Json.reads[Arrival]
}
