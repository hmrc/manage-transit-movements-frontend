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

import generated.{GNSSType, IncidentType03}
import models.{DynamicAddress, RichAddressType18}
import play.api.Logging
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section.StaticSection

import scala.concurrent.{ExecutionContext, Future}

class IncidentP5Helper(
  data: IncidentType03,
  refDataService: ReferenceDataService
)(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier)
    extends DeparturesP5MessageHelper
    with Logging {

  def incidentCodeRow: Future[Option[SummaryListRow]] =
    refDataService.getIncidentCode(data.code).map {
      incidentCode =>
        buildRowFromAnswer[String](
          answer = Some(incidentCode.toString),
          formatAnswer = formatAsText,
          prefix = "departure.notification.incident.index.code",
          id = None,
          call = None
        )
    }

  def incidentDescriptionRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(data.text),
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.description",
    id = None,
    call = None
  )

  def countryRow: Future[Option[SummaryListRow]] =
    refDataService
      .getCountry(data.Location.country)
      .map {
        country =>
          buildRowFromAnswer[String](
            answer = Some(country.toString),
            formatAnswer = formatAsText,
            prefix = "departure.notification.incident.index.country",
            id = None,
            call = None
          )
      }

  def identifierTypeRow: Future[Option[SummaryListRow]] =
    refDataService
      .getQualifierOfIdentification(data.Location.qualifierOfIdentification)
      .map {
        qualifierOfIdentification =>
          buildRowFromAnswer[String](
            answer = Some(qualifierOfIdentification.description),
            formatAnswer = formatAsText,
            prefix = "departure.notification.incident.index.identifierType",
            id = None,
            call = None
          )
      }

  def coordinatesRow: Option[SummaryListRow] =
    buildRowFromAnswer[GNSSType](
      answer = data.Location.GNSS,
      formatAnswer = formatAsCoordinates,
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
    id = None,
    call = None
  )

  def incidentInformationSection: Future[StaticSection] =
    for {
      country         <- countryRow
      incidentCodeRow <- incidentCodeRow
      identification  <- identifierTypeRow
    } yield StaticSection(
      sectionTitle = None,
      rows = Seq(
        incidentCodeRow,
        incidentDescriptionRow,
        identification,
        country,
        coordinatesRow,
        unLocodeRow,
        addressRow
      ).flatten
    )

  def transportEquipmentsSection: StaticSection = {
    val transportEquipmentsSections = data.TransportEquipment.map {
      transportEquipment =>
        val helper = new IncidentP5TransportEquipmentHelper(transportEquipment)
        helper.transportEquipmentSection
    }

    StaticSection(
      children = transportEquipmentsSections
    )
  }

}
