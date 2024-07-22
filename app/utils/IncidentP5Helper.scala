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

import generated.IncidentType03
import models.{DynamicAddress, RichAddressType18}
import play.api.Logging
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section.StaticSection

import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.{ExecutionContext, Future}

class IncidentP5Helper(
  data: IncidentType03,
  refDataService: ReferenceDataService
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends DeparturesP5MessageHelper
    with Logging {

  private val displayIndex = data.sequenceNumber

  def incidentCodeRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("code"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.code",
    id = None,
    call = None
  )

  def descriptionRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("description"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.description",
    id = None,
    call = None
  )

  def countryRow: Future[Option[SummaryListRow]] =
    refDataService.getCountry(data.Location.country) map {
      countryResponse =>
        val countryToDisplay = countryResponse.fold[String](identity, _.description)
        buildRowFromAnswer[String](
          answer = Some(countryToDisplay),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incident.index.country",
          id = Some(s"country-$displayIndex"),
          call = None
        )
    }

  def identifierTypeRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(data.Location.qualifierOfIdentification),
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.identifierType",
    id = Some(s"identifierType-$displayIndex"),
    call = None
  )

  def coordinatesRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("coordinates"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.coordinates",
    id = None,
    call = None
  )

  def addressRow: Option[SummaryListRow] =
    data.Location.Address.flatMap {
      address =>
        buildRowFromAnswer[DynamicAddress](
          answer = Some(address.toDynamicAddress),
          formatAnswer = formatAsDynamicAddress,
          prefix = "departure.notification.incident.index.address",
          id = None,
          call = None
        )
    }

  def unLocodeRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = data.Location.UNLocode,
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.unLocode",
    id = Some(s"unLocode-$displayIndex"),
    call = None
  )

  def incidentInformationSection: Future[StaticSection] =
    for {
      countryRowOption <- countryRow
    } yield StaticSection(
      sectionTitle = None,
      rows = Seq(
        incidentCodeRow,
        descriptionRow,
        countryRowOption,
        identifierTypeRow,
        coordinatesRow,
        unLocodeRow,
        addressRow
      ).flatten
    )

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

  def endorsementCountryRow: Future[Option[SummaryListRow]] =
    data.Endorsement
      .map {
        endorsementType =>
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
      }
      .getOrElse(Future.successful(None))

  def locationRow: Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Endorsement.map(_.place),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.location",
      id = None,
      call = None
    )

  def endorsementSection: Future[StaticSection] =
    for {
      endorsementCountryRow <- endorsementCountryRow
    } yield StaticSection(
      sectionTitle = Some(messages("departure.notification.incident.index.endorsement.section.title")),
      rows = Seq(
        endorsementDateRow,
        authorityRow,
        endorsementCountryRow,
        locationRow
      ).flatten
    )

  def identificationTypeRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("Identification type"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.identificationType",
    id = None,
    call = None
  )

  def identificationRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("Identification"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.identification",
    id = None,
    call = None
  )

  def registeredCountry: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("Registered Country"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.registeredCountry",
    id = None,
    call = None
  )

  def replacementMeansOfTransportSection: StaticSection = StaticSection(
    sectionTitle = Some(messages("departure.notification.incident.index.replacement.section.title")),
    rows = Seq(
      identificationTypeRow,
      identificationRow,
      registeredCountry
    ).flatten
  )

}
