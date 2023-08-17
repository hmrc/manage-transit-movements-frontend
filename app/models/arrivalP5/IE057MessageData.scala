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

import config.PaginationAppConfig
import models.departureP5.FunctionalError
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

case class IE057MessageData(
  transitOperation: TransitOperationIE057,
  customsOfficeOfDestinationActual: CustomsOfficeOfDestinationActual,
  functionalErrors: Seq[FunctionalError]
) {

  def pagedFunctionalErrors(page: Int)(implicit paginationAppConfig: PaginationAppConfig): Seq[FunctionalError] = {
    val start = (page - 1) * paginationAppConfig.arrivalsNumberOfErrorsPerPage
    functionalErrors
      .sortBy(_.errorCode)
      .slice(start, start + paginationAppConfig.arrivalsNumberOfErrorsPerPage)
  }

}

object IE057MessageData {

  implicit lazy val reads: Reads[IE057MessageData] = (
    (__ \ "TransitOperation").read[TransitOperationIE057] and
      (__ \ "CustomsOfficeOfDestinationActual").read[CustomsOfficeOfDestinationActual] and
      (__ \ "FunctionalError").readWithDefault[Seq[FunctionalError]](Nil)
  )(IE057MessageData.apply _)
}
