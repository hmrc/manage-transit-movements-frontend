/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import models.LocalReferenceNumber
import models.departure.ControlDecision
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.ControlDecisionView

import java.time.LocalDate

class ControlDecisionViewSpec extends ViewBehaviours {

  private def applyView(controlDecisionMessage: ControlDecision, lrn: LocalReferenceNumber): HtmlFormat.Appendable =
    injector.instanceOf[ControlDecisionView].apply(controlDecisionMessage, lrn)(fakeRequest, messages)

  private val controlDecision  = ControlDecision("mrnValue", LocalDate.now(), "principleTraderNameValue", Some("eoriValue"))
  private val controlDecision1 = ControlDecision("mrnValue", LocalDate.now(), "principleTraderNameValue", None)

  override val prefix: String = "controlDecision"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  override def view: HtmlFormat.Appendable = applyView(controlDecision, lrn)

  "must return eori number if available and hide trader name" - {}

//  "ControlDecisionView" - {
//
//    "must return eori number if available and hide trader name" in {
//      val setControlDecision = ControlDecision("mrnValue", LocalDate.now(), "principleTraderNameValue", Some("eoriValue"))
//
//      val doc: Document = renderDocument(
//        Json.obj("controlDecisionMessage" -> Json.toJson(setControlDecision), "lrn" -> lrn.value)
//      ).futureValue
//
//      val getSummaryElements = doc.getElementsByClass("govuk-summary-list__row")
//
//      getSummaryElements.size mustBe 4
//
//      getSummaryElements.eq(0).select("dd").text mustBe setControlDecision.movementReferenceNumber
//      getSummaryElements.eq(1).select("dd").text mustBe lrn.value
//      getSummaryElements.eq(2).select("dd").text mustBe setControlDecision.principleEori.value
//      getSummaryElements.eq(3).select("dd").text mustBe Format.controlDecisionDateFormatted(setControlDecision.dateOfControl)
//    }
//
//    "must return trader name if eori number is unavailable" in {
//      val setControlDecision = ControlDecision("mrnValue", LocalDate.now(), "principleTraderNameValue", None)
//
//      val doc: Document = renderDocument(
//        Json.obj("controlDecisionMessage" -> Json.toJson(setControlDecision), "lrn" -> lrn.value)
//      ).futureValue
//
//      val getSummaryElements = doc.getElementsByClass("govuk-summary-list__row")
//
//      getSummaryElements.size mustBe 4
//
//      getSummaryElements.eq(0).select("dd").text mustBe setControlDecision.movementReferenceNumber
//      getSummaryElements.eq(1).select("dd").text mustBe lrn.value
//      getSummaryElements.eq(2).select("dd").text mustBe setControlDecision.principleTraderName
//      getSummaryElements.eq(3).select("dd").text mustBe Format.controlDecisionDateFormatted(setControlDecision.dateOfControl)
//    }
//  }

}
