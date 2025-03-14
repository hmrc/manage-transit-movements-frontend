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

package viewModels

import utils.Format

import java.time.{LocalDate, LocalTime}

trait ViewMovement {
  val updatedDate: LocalDate
  val updatedTime: LocalTime
  val referenceNumber: String
  val status: String
  val actions: Seq[ViewMovementAction]

  val updated: String = Format.formatMovementUpdatedTime(updatedTime)
}

object ViewMovement {

  implicit class RichViewMovements[T <: ViewMovement](value: Seq[T]) {

    def groupByDate: Seq[(String, Seq[T])] = {

      implicit val dateOrdering: Ordering[(LocalDate, Seq[T])] =
        Ordering.by[(LocalDate, Seq[T]), LocalDate](_._1).reverse

      implicit val timeOrdering: Ordering[T] =
        Ordering.by[T, LocalTime](_.updatedTime).reverse

      value
        .groupBy(_.updatedDate)
        .toSeq
        .sorted
        .map {
          result =>
            (Format.formatMovementUpdatedDate(result._1), result._2.sorted)
        }
    }
  }
}
