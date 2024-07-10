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

package viewModels.P5.departure

import generated.CC182CType
import models.Index
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import utils.IncidentP5Helper
import viewModels.P5.ViewModelWithCustomsOffice
import viewModels.sections.Section

case class IncidentP5ViewModel(
  lrn: String,
  customsOffice: Either[String, CustomsOffice],
  isMultipleIncidents: Boolean,
  sections: Seq[Section],
  incidentIndex: Index
) extends ViewModelWithCustomsOffice {

  override val prefix: String = "departure.notification.incident.index.customsOfficeContact"

  def title(implicit messages: Messages): String =
    messages("departure.notification.incident.index.title", incidentIndex.display)

  def heading(implicit messages: Messages): String =
    messages("departure.notification.incident.index.heading", incidentIndex.display)

  def paragraph1(implicit messages: Messages): String = if (isMultipleIncidents) {
    messages("departure.notification.incident.index.multi.paragraph1")
  } else {
    messages("departure.notification.incident.index.paragraph1")
  }

}

object IncidentP5ViewModel {

  class IncidentP5ViewModelProvider {

    def apply(
      data: CC182CType,
      referenceNumbers: DepartureReferenceNumbers,
      customsOffice: Either[String, CustomsOffice],
      isMultipleIncidents: Boolean,
      incidentIndex: Index
    )(implicit messages: Messages): IncidentP5ViewModel = {

      val helper = new IncidentP5Helper(data, isMultipleIncidents)

      val sections = Seq(
        helper.incidentInformationSection,
        helper.endorsementSection,
        helper.replacementMeansOfTransportSection
      )

      IncidentP5ViewModel(
        referenceNumbers.localReferenceNumber.value,
        customsOffice,
        isMultipleIncidents,
        sections,
        incidentIndex
      )
    }
  }

}