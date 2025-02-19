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

import cats.implicits.toTraverseOps
import generated.CC182CType
import models.Index
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IncidentEndorsementP5Helper, IncidentP5Helper, IncidentP5TranshipmentHelper}
import viewModels.P5.ViewModelWithCustomsOffice
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import scala.concurrent.{ExecutionContext, Future}

case class IncidentP5ViewModel(
  lrn: String,
  customsOffice: Option[CustomsOffice],
  isMultipleIncidents: Boolean,
  sections: Seq[Section],
  customsOfficeId: String,
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
      referenceDataService: ReferenceDataService,
      referenceNumbers: DepartureReferenceNumbers,
      customsOffice: Option[CustomsOffice],
      isMultipleIncidents: Boolean,
      customsOfficeId: String,
      incidentIndex: Index
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[IncidentP5ViewModel] = {

      val incident = data.Consignment.Incident(incidentIndex.position)
      val helper   = new IncidentP5Helper(incident, referenceDataService)

      val incidentInformationSectionFuture = helper.incidentInformationSection
      val transhipmentSectionFuture: Future[Option[StaticSection]] =
        incident.Transhipment.map {
          transhipment =>
            val helper = new IncidentP5TranshipmentHelper(transhipment, referenceDataService)
            helper.replacementMeansOfTransportSection
        }.sequence

      val endorsementSectionFuture: Future[Option[StaticSection]] =
        incident.Endorsement.map {
          endorsement =>
            val helper = new IncidentEndorsementP5Helper(endorsement, referenceDataService)
            helper.endorsementSection
        }.sequence

      for {
        incidentInformationSection <- incidentInformationSectionFuture
        transhipmentSection        <- transhipmentSectionFuture
        endorsementSection         <- endorsementSectionFuture
      } yield {
        val sections = Seq(
          Some(incidentInformationSection),
          endorsementSection,
          Some(helper.transportEquipmentsSection),
          transhipmentSection
        ).flatten

        IncidentP5ViewModel(
          referenceNumbers.localReferenceNumber,
          customsOffice,
          isMultipleIncidents,
          sections,
          customsOfficeId,
          incidentIndex
        )
      }

    }

  }

}
