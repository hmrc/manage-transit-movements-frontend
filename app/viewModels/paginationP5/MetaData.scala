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

package viewModels.paginationP5

case class MetaData(from: Int, to: Int, count: Int, currentPage: Int, totalPages: Int)

object MetaData {

  def apply(totalNumberOfMovements: Int, numberOfMovementsPerPage: Int, currentPage: Int): MetaData = {

    val numberOfPagesFloat = totalNumberOfMovements.toFloat / numberOfMovementsPerPage

    val totalNumberOfPages = if (numberOfPagesFloat.isWhole) {
      numberOfPagesFloat.toInt
    } else {
      numberOfPagesFloat.toInt + 1
    }

    val from = numberOfMovementsPerPage * (currentPage - 1) + 1

    val to = if (currentPage == 1) {
      numberOfMovementsPerPage
    } else {
      val roundedNumberOfMovementsPerPage = numberOfMovementsPerPage * currentPage

      if (roundedNumberOfMovementsPerPage > totalNumberOfMovements) {
        totalNumberOfMovements
      } else {
        roundedNumberOfMovementsPerPage
      }
    }

    MetaData(from, to, totalNumberOfMovements, currentPage, totalNumberOfPages)
  }
}
