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

package viewModels

import utils.Format

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate

trait ViewMovements {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def format[T <: ViewMovement](movements: Seq[T]): Seq[(String, Seq[T])] = {
    val groupMovements: Map[LocalDate, Seq[T]] =
      movements.groupBy(_.updatedDate)
    val sortByDate: Seq[(LocalDate, Seq[T])] =
      groupMovements.toSeq.sortBy(_._1).reverse

    sortByDate.map {
      result =>
        (result._1.format(Format.dateDisplayFormat), result._2.sortBy(_.updatedTime).reverse)
    }
  }

}
