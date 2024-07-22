/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IncidentP5MessageHelper(
  referenceDataService: ReferenceDataService
)(implicit
  messages: Messages,
  ec: ExecutionContext,
  hc: HeaderCarrier
) extends DeparturesP5MessageHelper {

  def incidentCodeRow(incidentCode: String, prefix: String): Future[Option[SummaryListRow]] =
    referenceDataService.getIncidentCode(incidentCode).map {
      incidentCode =>
        buildRowFromAnswer[String](
          answer = Some(incidentCode.toString),
          formatAnswer = formatAsText,
          prefix = prefix,
          id = None,
          call = None
        )
    }

  def incidentDescriptionRow(incidentText: String, prefix: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(incidentText),
    formatAnswer = formatAsText,
    prefix = prefix,
    id = None,
    call = None
  )

}
