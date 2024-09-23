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

import generated.CC009CType
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.DepartureCancelledP5Helper
import viewModels.P5.ViewModelWithCustomsOffice
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class DepartureCancelledP5ViewModel(
  sections: Seq[Section],
  lrn: String,
  customsOffice: Either[String, CustomsOffice]
) extends ViewModelWithCustomsOffice {

  override val prefix: String = "departure.cancelled.customsOfficeContact"

  def title(implicit messages: Messages): String = messages("departure.cancelled.title")

  def heading(implicit messages: Messages): String = messages("departure.cancelled.heading")

  def paragraph(implicit messages: Messages): String = messages("departure.cancelled.paragraph", lrn)

  def hyperlink(implicit messages: Messages): String = messages("departure.cancelled.hyperlink")

  def caption(implicit messages: Messages): String = messages("departure.messages.caption", lrn)
}

object DepartureCancelledP5ViewModel {

  class DepartureCancelledP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie009: CC009CType,
      lrn: String,
      customsOffice: Either[String, CustomsOffice]
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureCancelledP5ViewModel] = {
      val helper = new DepartureCancelledP5Helper(ie009, referenceDataService)

      helper.buildInvalidationSection.map {
        section =>
          new DepartureCancelledP5ViewModel(Seq(section), lrn, customsOffice)
      }
    }

  }

}
