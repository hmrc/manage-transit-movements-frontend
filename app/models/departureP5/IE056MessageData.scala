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

import config.PaginationAppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

case class IE056MessageData(
  transitOperation: TransitOperationIE056,
  customsOfficeOfDeparture: CustomsOfficeOfDeparture,
  functionalErrors: Seq[FunctionalError]
) {

  def pagedFunctionalErrors(page: Int)(implicit paginationAppConfig: PaginationAppConfig): Seq[FunctionalError] = {
    val start = (page - 1) * paginationAppConfig.departuresNumberOfErrorsPerPage
    functionalErrors
      .sortBy(_.errorCode)
      .slice(start, start + paginationAppConfig.departuresNumberOfErrorsPerPage)
  }

}

object IE056MessageData {

  implicit lazy val reads: Reads[IE056MessageData] = (
    (__ \ "TransitOperation").read[TransitOperationIE056] and
      (__ \ "CustomsOfficeOfDeparture").read[CustomsOfficeOfDeparture] and
      (__ \ "FunctionalError").readWithDefault[Seq[FunctionalError]](Nil)
  )(IE056MessageData.apply _)
}
