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

import generated.CC182CType
import models.Index
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section.StaticSection

class IncidentP5Helper(
  data: CC182CType,
  isMultipleIncidents: Boolean
)(implicit messages: Messages)
    extends DeparturesP5MessageHelper {

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

  def countryRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("GB"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.country",
    id = None,
    call = None
  )

  def identifierTypeRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("identifierType"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.identifierType",
    id = None,
    call = None
  )

  def coordinatesRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("coordinates"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.coordinates",
    id = None,
    call = None
  )

  def incidentInformationSection: StaticSection = StaticSection(
    sectionTitle = None,
    rows = Seq(
      incidentCodeRow,
      descriptionRow,
      countryRow,
      identifierTypeRow,
      coordinatesRow
    ).flatten
  )

  def endorsementDateRow(incidentIndex: Index): Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Consignment.Incident(incidentIndex.position).Endorsement.map(_.date.toString),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.endorsement",
      id = None,
      call = None
    )

  def authorityRow(incidentIndex: Index): Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Consignment.Incident(incidentIndex.position).Endorsement.map(_.authority),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.authority",
      id = None,
      call = None
    )

  def endorsementCountryRow(incidentIndex: Index): Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Consignment.Incident(incidentIndex.position).Endorsement.map(_.country),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.endorsementCountry",
      id = None,
      call = None
    )

  def locationRow(incidentIndex: Index): Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = data.Consignment.Incident(incidentIndex.position).Endorsement.map(_.place),
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.location",
      id = None,
      call = None
    )

  def endorsementSection(incidentIndex: Index): StaticSection = StaticSection(
    sectionTitle = Some(messages("departure.notification.incident.index.endorsement.section.title")),
    rows = Seq(
      endorsementDateRow(incidentIndex),
      authorityRow(incidentIndex),
      endorsementCountryRow(incidentIndex),
      locationRow(incidentIndex)
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
