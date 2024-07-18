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

import generated.TransportEquipmentType07
import models.Index
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section.{AccordionSection, StaticSection}

class IncidentP5TransportEquipmentHelper(
  data: Seq[TransportEquipmentType07]
)(implicit messages: Messages)
    extends DeparturesP5MessageHelper
    with Logging {

  def containerIdentificationNumberRow(equipmentIndex: Index): Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = Some(data(equipmentIndex.position).containerIdentificationNumber).flatten,
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.transportEquipment.containerIdentificationNumber",
      id = None,
      call = None
    )

  def transportEquipmentSection(equipmentIndex: Index): AccordionSection =
    AccordionSection(
      sectionTitle = Some(messages("departure.notification.incident.index.transportEquipment.heading", equipmentIndex.display)),
      rows = Seq(containerIdentificationNumberRow(equipmentIndex)).flatten,
      isOpen = if (equipmentIndex.position == 0) true else false
    )

  def transportEquipmentsSection: StaticSection = {
    val transportEquipmentsSections = data.zipWithIndex.map {
      case (_, index) => transportEquipmentSection(Index(index))
    }

    StaticSection(
      children = transportEquipmentsSections
    )
  }

}
