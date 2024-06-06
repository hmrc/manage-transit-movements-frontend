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

import generated.CC060CType
import models.RichCC060Type
import play.api.i18n.Messages
import play.api.mvc.Call
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.GoodsUnderControlP5MessageHelper
import viewModels.sections.Section

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class GoodsUnderControlP5ViewModel(sections: Seq[Section], requestedDocuments: Boolean, lrn: Option[String]) {

  def title(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie060.message.requestedDocuments.title")
  } else {
    messages("departure.ie060.message.title")
  }

  def heading(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie060.message.requestedDocuments.heading")
  } else {
    messages("departure.ie060.message.heading")
  }

  def paragraph1(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie060.message.requestedDocuments.paragraph1")
  } else {
    messages("departure.ie060.message.paragraph1")
  }

  def paragraph2(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie060.message.requestedDocuments.paragraph2")
  } else {
    messages("departure.ie060.message.paragraph2")
  }

  def paragraph3(implicit messages: Messages): String = if (requestedDocuments) {
    messages("departure.ie060.message.requestedDocuments.paragraph3")
  } else {
    messages("departure.ie060.message.paragraph3")
  }

  def type0LinkPrefix(implicit messages: Messages): String = messages("departure.ie060.message.paragraph4.prefix")

  val type0ParagraphLink: Call                                 = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None)
  def type0LinkText(implicit messages: Messages): String       = messages("departure.ie060.message.paragraph4.linkText")
  def type0LinkTextSuffix(implicit messages: Messages): String = messages("departure.ie060.message.paragraph4.suffix")

}

object GoodsUnderControlP5ViewModel {

  class GoodsUnderControlP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      ie060: CC060CType
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[GoodsUnderControlP5ViewModel] = {
      val helper = new GoodsUnderControlP5MessageHelper(ie060, referenceDataService)

      helper.buildGoodsUnderControlSection().flatMap {
        goodsUnderControlSection =>
          helper.controlInformationSection().flatMap {
            controlInfoSections =>
              helper.documentSection().map {
                documentSection =>
                  val sections = ie060.TransitOperation.notificationType match {
                    case "1" => Seq(goodsUnderControlSection) ++ documentSection
                    case _   => Seq(goodsUnderControlSection) ++ controlInfoSections ++ documentSection
                  }
                  new GoodsUnderControlP5ViewModel(sections, ie060.informationRequested, ie060.TransitOperation.LRN)
              }
          }
      }
    }
  }
}
