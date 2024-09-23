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

package viewModels.P5.departure

import generated.CC182CType
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.IncidentsDuringTransitP5Helper
import viewModels.P5.ViewModelWithCustomsOffice
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class IncidentsDuringTransitP5ViewModel(
  lrn: String,
  customsOffice: Either[String, CustomsOffice],
  isMultipleIncidents: Boolean,
  sections: Seq[Section]
) extends ViewModelWithCustomsOffice {

  override val prefix: String = "departure.notification.incidents.customsOfficeContact"

  def title(implicit messages: Messages): String = if (isMultipleIncidents) {
    messages("departure.notification.incidents.title")
  } else {
    messages("departure.notification.incident.title")
  }

  def heading(implicit messages: Messages): String = if (isMultipleIncidents) {
    messages("departure.notification.incidents.heading")
  } else {
    messages("departure.notification.incident.heading")
  }

  def paragraph1(implicit messages: Messages): String = if (isMultipleIncidents) {
    messages("departure.notification.incidents.paragraph1")
  } else {
    messages("departure.notification.incident.paragraph1")
  }

  def paragraph2(implicit messages: Messages): String = if (isMultipleIncidents) {
    messages("departure.notification.incidents.paragraph2")
  } else {
    messages("departure.notification.incident.paragraph2")
  }

  def paragraph3HyperLink(implicit messages: Messages): String = messages("departure.notification.incidents.paragraph3.hyperlink")

  def paragraph3End(implicit messages: Messages): String = messages("departure.notification.incidents.paragraph3.end")

  def whatHappensNextHeader(implicit messages: Messages): String = messages("departure.notification.incidents.whatHappensNextHeader")

}

object IncidentsDuringTransitP5ViewModel {

  class IncidentsDuringTransitP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      departureId: String,
      messageId: String,
      data: CC182CType,
      referenceNumbers: DepartureReferenceNumbers,
      customsOffice: Either[String, CustomsOffice],
      isMultipleIncidents: Boolean
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentsDuringTransitP5ViewModel] = {

      val helper = new IncidentsDuringTransitP5Helper(data, isMultipleIncidents, referenceDataService)

      for {
        incidentInformationSection <- helper.incidentInformationSection
        incidentsSection           <- helper.incidentsSection(departureId, messageId)
        sections = Seq(incidentInformationSection, incidentsSection)
      } yield IncidentsDuringTransitP5ViewModel(
        referenceNumbers.localReferenceNumber,
        customsOffice,
        isMultipleIncidents,
        sections
      )
    }
  }

}
