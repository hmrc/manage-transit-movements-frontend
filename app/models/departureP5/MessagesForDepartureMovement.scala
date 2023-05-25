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

package models.departureP5

import cats.data.NonEmptyList
import models.departureP5.DepartureMessageType.RejectedByOfficeOfDeparture
import play.api.libs.json.{Json, JsonValidationError, Reads}

case class MessagesForDepartureMovement(messages: NonEmptyList[DepartureMessage]) {
  val messageBeforeLatest: Option[DepartureMessage] = messages.tail.headOption
}

object MessagesForDepartureMovement {

  implicit val sortedNonEmptyListReads: Reads[NonEmptyList[DepartureMessage]] =
    Reads
      .of[List[DepartureMessage]]
      .collect(
        JsonValidationError("expected a NonEmptyList but the list was empty")
      ) {
        list =>
          val sortedList: List[DepartureMessage] = list.sortBy(_.received).reverse

          NonEmptyList[DepartureMessage](sortedList.head, sortedList.tail)
      }

  implicit val reads: Reads[MessagesForDepartureMovement] = Json.reads[MessagesForDepartureMovement]
}
