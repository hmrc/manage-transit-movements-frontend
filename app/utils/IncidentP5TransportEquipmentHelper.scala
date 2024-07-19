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
import viewModels.sections.Section.AccordionSection

class IncidentP5TransportEquipmentHelper(
  data: TransportEquipmentType07
)(implicit messages: Messages)
    extends DeparturesP5MessageHelper
    with Logging {

  import data._

  def containerIdentificationNumberRow: Option[SummaryListRow] =
    buildRowFromAnswer[String](
      answer = Some(containerIdentificationNumber).flatten,
      formatAnswer = formatAsText,
      prefix = "departure.notification.incident.index.transportEquipment.containerIdentificationNumber",
      id = None,
      call = None
    )

  def goodsReferenceNumber(goodsReferenceNumber: BigInt, index: Index): Option[SummaryListRow] = buildRowFromAnswer[BigInt](
    answer = Some(goodsReferenceNumber),
    formatAnswer = formatAsText,
    prefix = "departure.notification.incident.index.goodsReference.referenceNumber",
    id = None,
    call = None,
    args = index.display
  )

  def goodsReferenceSection: AccordionSection = {
    val rows = data.GoodsReference.zipWithIndex.map {
      case (goodsReference, index) =>
        goodsReferenceNumber(goodsReference.declarationGoodsItemNumber, Index(index))
    }

    AccordionSection(
      sectionTitle = Some(messages("departure.notification.incident.index.goodsReference.section.title")),
      rows = rows.flatten
    )
  }

  def transportEquipmentSection: AccordionSection =
    AccordionSection(
      sectionTitle = Some(messages("departure.notification.incident.index.transportEquipment.heading", sequenceNumber)),
      rows = Seq(
        containerIdentificationNumberRow
      ).flatten,
      isOpen = sequenceNumber == "1"
    )

}
