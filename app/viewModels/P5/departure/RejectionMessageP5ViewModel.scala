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

import models.departureP5.IE056MessageData
import play.api.i18n.Messages
import play.api.mvc.Call
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RejectionMessageP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class RejectionMessageP5ViewModel(sections: Seq[Section], requestedDocuments: Boolean, lrn: Option[String]) {

  def title(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie056.message.requestedDocuments.title")
  } else {
    messages("departure.ie056.message.title")
  }

  def heading(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie056.message.requestedDocuments.heading")
  } else {
    messages("departure.ie056.message.heading")
  }

  def paragraph1(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie056.message.requestedDocuments.paragraph1")
  } else {
    messages("departure.ie056.message.paragraph1")
  }

  def paragraph2(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie056.message.requestedDocuments.paragraph2")
  } else {
    messages("departure.ie056.message.paragraph2")
  }

  def paragraph3(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie056.message.requestedDocuments.paragraph3")
  } else {
    messages("departure.ie056.message.paragraph3")
  }

  def type0LinkPrefix(implicit messages: Messages): String = messages("departure.ie056.message.paragraph4.prefix")

  val type0ParagraphLink: Call                                 = controllers.testOnly.routes.ViewAllDeparturesP5Controller.onPageLoad()
  def type0LinkText(implicit messages: Messages): String       = messages("departure.ie056.message.paragraph4.linkText")
  def type0LinkTextSuffix(implicit messages: Messages): String = messages("departure.ie056.message.paragraph4.suffix")

}

object RejectionMessageP5ViewModel {

  class RejectionMessageP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie056MessageData: IE056MessageData
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[RejectionMessageP5ViewModel] = {
      val helper = new RejectionMessageP5MessageHelper(ie056MessageData, referenceDataService)

      val notificationType: String    = ie056MessageData.TransitOperation.notificationType
      val requestedDocuments: Boolean = ie056MessageData.requestedDocumentsToSeq.nonEmpty || notificationType == "1"
      val lrn                         = ie056MessageData.TransitOperation.LRN

      helper.controlInformationSection().map {
        controlInfoSections =>
          val sections = notificationType match {
            case "1" => Seq(helper.buildRejectionMessageSection()) ++ helper.documentSection()
            case _   => Seq(helper.buildRejectionMessageSection()) ++ controlInfoSections ++ helper.documentSection()
          }

          new RejectionMessageP5ViewModel(sections, requestedDocuments, lrn)
      }
    }
  }
}
