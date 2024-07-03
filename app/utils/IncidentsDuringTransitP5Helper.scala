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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section.StaticSection

class IncidentsDuringTransitP5Helper(
  data: CC182CType
)(implicit messages: Messages)
    extends DeparturesP5MessageHelper {

  def mrnRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(data.TransitOperation.MRN),
    formatAnswer = formatAsText,
    prefix = "arrival.notification.incidents.label.mrn",
    id = None,
    call = None
  )

  def incidentInformationSection: StaticSection = StaticSection(
    sectionTitle = None,
    rows = Seq(
      mrnRow
    ).flatten
  )
}
