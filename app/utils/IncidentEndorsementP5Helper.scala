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

import generated.{EndorsementType03, IncidentType03}
import play.api.Logging
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier

import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.{ExecutionContext, Future}

class IncidentEndorsementP5Helper(
  data: IncidentType03,
  refDataService: ReferenceDataService
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends DeparturesP5MessageHelper
    with Logging {

  private val displayIndex = data.sequenceNumber

  def endorsementDateRow: Option[SummaryListRow] =
    buildRowFromAnswer[XMLGregorianCalendar](
      answer = data.Endorsement.map(_.date),
      formatAnswer = formatAsDate,
      prefix = "departure.notification.incident.index.endorsement",
      id = None,
      call = None
    )

  def authorityRow: Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Endorsement.map(_.authority),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.authority",
      id = None,
      call = None
    )

  def endorsementCountryRow(endorsementType: EndorsementType03): Future[Option[SummaryListRow]] =
    refDataService.getCountry(endorsementType.country) map {
      countryResponse =>
        val countryToDisplay = countryResponse.fold[String](identity, _.description)
        buildRowFromAnswer[String](
          answer = Some(countryToDisplay),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incident.index.endorsementCountry",
          id = Some(s"country-$displayIndex"),
          call = None
        )
    }

  def locationRow: Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Endorsement.map(_.place),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.location",
      id = None,
      call = None
    )

}
