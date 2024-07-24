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

import generated.TranshipmentType02
import play.api.Logging
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section.StaticSection

import scala.concurrent.{ExecutionContext, Future}

class IncidentP5TranshipmentHelper(
  transhipment: TranshipmentType02,
  refDataService: ReferenceDataService
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends DeparturesP5MessageHelper
    with Logging {

  def identificationRow: Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = Some(transhipment.TransportMeans.identificationNumber),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.identification",
      id = None,
      call = None
    )

  def identificationTypeRow: Future[Option[SummaryListRow]] =
    refDataService.getIdentificationType(transhipment.TransportMeans.typeOfIdentification) map {
      identificationTypeResponse =>
        val identificationType = identificationTypeResponse.fold[String](identity, _.description)
        buildRowFromAnswer[String](
          answer = Some(identificationType),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incident.index.identificationType",
          id = None,
          call = None
        )
    }

  def registeredCountryRow: Future[Option[SummaryListRow]] =
    refDataService.getNationality(transhipment.TransportMeans.nationality) map {
      nationalityResponse =>
        val nationalityToDisplay = nationalityResponse.fold[String](identity, _.description)
        buildRowFromAnswer[String](
          answer = Some(nationalityToDisplay),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incident.index.registeredCountry",
          id = None,
          call = None
        )
    }

  def replacementMeansOfTransportSection: Future[StaticSection] =
    for {
      registeredCountry  <- registeredCountryRow
      identificationType <- identificationTypeRow
    } yield StaticSection(
      sectionTitle = Some(messages("departure.notification.incident.index.replacement.section.title")),
      rows = Seq(
        identificationType,
        identificationRow,
        registeredCountry
      ).flatten
    )

}
