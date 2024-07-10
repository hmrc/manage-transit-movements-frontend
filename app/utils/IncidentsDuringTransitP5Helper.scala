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

package utils

import generated.CC182CType
import models.Link
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section.{AccordionSection, StaticSection}

import scala.concurrent.{ExecutionContext, Future}

class IncidentsDuringTransitP5Helper(
  data: CC182CType,
  isMultipleIncidents: Boolean,
  referenceDataService: ReferenceDataService
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends DeparturesP5MessageHelper {

  def mrnRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(data.TransitOperation.MRN),
    formatAnswer = formatAsText,
    prefix = "arrival.notification.incidents.label.mrn",
    id = None,
    call = None
  )

  def dateTimeIncidentReportedRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("time of incident here"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix =
      if (isMultipleIncidents) "arrival.notification.incidents.label.dateAndTime.plural" else "arrival.notification.incidents.label.dateAndTime.singular",
    id = None,
    call = None
  )

  def customsOfficeOfIncidentRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("customs office of incident"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "arrival.notification.incidents.label.officeOfIncident",
    id = None,
    call = None
  )

  def officeOfDepartureRow: Future[Option[SummaryListRow]] = {
    val referenceNumber = data.CustomsOfficeOfDeparture.referenceNumber
    referenceDataService.getCustomsOffice(referenceNumber).map {
      customsOffice =>
        val answerToDisplay = customsOffice match {
          case Right(customsOffice) => customsOffice.nameAndCode
          case Left(id)             => id
        }
        buildRowFromAnswer[String](
          answer = Some(answerToDisplay),
          formatAnswer = formatAsText,
          prefix = "arrival.notification.incidents.label.officeOfDeparture",
          id = None,
          call = None
        )
    }
  }

  def incidentCodeRow(incidentIndex: Int): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("incident code here"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "arrival.notification.incidents.incident.code.label",
    id = None,
    call = None
  )

  def incidentDescriptionRow(incidentIndex: Int): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some("incident description here"), // TODO: Pull from incident data
    formatAnswer = formatAsText,
    prefix = "arrival.notification.incidents.incident.description.label",
    id = None,
    call = None
  )

  def incidentInformationSection: Future[StaticSection] =
    for {
      officeOfDepartureRow <- officeOfDepartureRow
      rows = Seq(mrnRow, dateTimeIncidentReportedRow, customsOfficeOfIncidentRow, officeOfDepartureRow).flatten
    } yield StaticSection(
      sectionTitle = None,
      rows = rows
    )

  def incidentsSection: AccordionSection = AccordionSection(
    sectionTitle = Some(messages("arrival.notification.incidents.heading.incident")),
    children = data.Consignment.Incident.zipWithIndex.map {
      case (_, index) => incidentSection(index)
    },
    isOpen = true
  )

  def incidentSection(incidentIndex: Int): AccordionSection = AccordionSection(
    sectionTitle = messages("arrival.notification.incidents.subheading.incident", incidentIndex.toDisplay),
    rows = Seq(
      incidentCodeRow(incidentIndex),
      incidentDescriptionRow(incidentIndex)
    ).flatten,
    isOpen = if (incidentIndex == 0) true else false,
    viewLinks = Seq(
      Link(
        id = s"more-details-incident-${incidentIndex.toDisplay}",
        text = messages("arrival.notification.incidents.link"),
        href = controllers.routes.SessionExpiredController.onPageLoad().url, //TODO: Update navigation to incident page once implemented
        visuallyHidden = Some(messages("arrival.notification.incidents.link.hidden", incidentIndex.toDisplay))
      )
    )
  )

}
