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
import models.{Index, Link}
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section.{AccordionSection, StaticSection}

import javax.xml.datatype.XMLGregorianCalendar
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
    prefix = "departure.notification.incidents.label.mrn",
    id = None,
    call = None
  )

  def dateTimeIncidentReportedRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = Some(data.TransitOperation.incidentNotificationDateAndTime),
    formatAnswer = formatAsIncidentDateTime,
    prefix =
      if (isMultipleIncidents) "departure.notification.incidents.label.dateAndTime.plural" else "departure.notification.incidents.label.dateAndTime.singular",
    id = None,
    call = None
  )

  def customsOfficeOfIncidentRow: Future[Option[SummaryListRow]] = {
    val referenceNumber = data.CustomsOfficeOfIncidentRegistration.referenceNumber
    referenceDataService.getCustomsOffice(referenceNumber).map {
      customsOffice =>
        buildRowFromAnswer[String](
          answer = Some(customsOffice.nameAndCode),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incidents.label.officeOfIncident",
          id = None,
          call = None
        )
    }
  }

  def officeOfDepartureRow: Future[Option[SummaryListRow]] = {
    val referenceNumber = data.CustomsOfficeOfDeparture.referenceNumber
    referenceDataService.getCustomsOffice(referenceNumber).map {
      customsOffice =>
        buildRowFromAnswer[String](
          answer = Some(customsOffice.nameAndCode),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incidents.label.officeOfDeparture",
          id = None,
          call = None
        )
    }
  }

  def incidentInformationSection: Future[StaticSection] =
    for {
      officeOfDepartureRow       <- officeOfDepartureRow
      customsOfficeOfIncidentRow <- customsOfficeOfIncidentRow
      rows = Seq(mrnRow, dateTimeIncidentReportedRow, customsOfficeOfIncidentRow, officeOfDepartureRow).flatten
    } yield StaticSection(
      sectionTitle = None,
      rows = rows
    )

  def incidentsSection(departureId: String, messageId: String): Future[AccordionSection] = {
    val incidentSections: Seq[Future[AccordionSection]] = data.Consignment.Incident.zipWithIndex.map {
      case (_, index) => incidentSection(departureId, Index(index), messageId)
    }

    Future.sequence(incidentSections).map {
      sections =>
        AccordionSection(
          sectionTitle = Some(messages("departure.notification.incidents.heading.incident")),
          children = sections,
          isOpen = true
        )
    }
  }

  def incidentSection(departureId: String, incidentIndex: Index, messageId: String): Future[AccordionSection] = {
    val incidentHelper = new IncidentP5Helper(data.Consignment.Incident(incidentIndex.position), referenceDataService)
    incidentHelper.incidentCodeRow.map {
      incidentCode =>
        AccordionSection(
          sectionTitle = messages("departure.notification.incidents.subheading.incident", incidentIndex.display),
          rows = Seq(
            incidentCode,
            incidentHelper.incidentDescriptionRow
          ).flatten,
          isOpen = incidentIndex.position == 0,
          viewLinks = Seq(
            Link(
              id = s"more-details-incident-${incidentIndex.display}",
              text = messages("departure.notification.incidents.link"),
              href = controllers.departureP5.routes.IncidentP5Controller.onPageLoad(departureId, incidentIndex, messageId).url,
              visuallyHidden = Some(messages("departure.notification.incidents.link.hidden", incidentIndex.display))
            )
          )
        )
    }
  }
}
