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

package models.arrivalP5

import cats.data.NonEmptyList
import models.arrivalP5.ArrivalMessageType.{
  FunctionalNack,
  FunctionalNackArrival,
  FunctionalNackUnloading,
  UnloadingRemarks,
  XmlNack,
  XmlNackArrival,
  XmlNackUnloading
}
import play.api.libs.json.{Json, JsonValidationError, Reads}

case class MessagesForMovement(messages: NonEmptyList[Message]) {
  val messageBeforeLatest: Option[Message] = messages.tail.headOption
}

object MessagesForMovement {

  implicit val sortedNonEmptyListReads: Reads[NonEmptyList[Message]] =
    Reads
      .of[List[Message]]
      .collect(
        JsonValidationError("expected a NonEmptyList but the list was empty")
      ) {
        list =>
          val sortedList: List[Message] = list.sortBy(_.received).reverse

          val getFirstMessage: Message = sortedList.head.messageType match {
            case FunctionalNack =>
              if (sortedList.tail.exists(_.messageType == UnloadingRemarks)) {
                sortedList.head.copy(messageType = FunctionalNackUnloading)
              } else {
                sortedList.head.copy(messageType = FunctionalNackArrival)
              }
            case _ => sortedList.head
          }

          NonEmptyList[Message](getFirstMessage, sortedList.tail)
      }

  implicit val reads: Reads[MessagesForMovement] = Json.reads[MessagesForMovement]
}
