/*
 * Copyright 2021 HM Revenue & Customs
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

import base.ViewSpecBase
import generators.Generators
import models.LocalReferenceNumber
import models.departure.ControlDecision
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import utils.Format

class ControlDecisionViewSpec extends ViewSpecBase with Generators with ScalaCheckPropertyChecks {

  "ControlDecisionView" - {

    "must return eori number if available and hide trader name" in {

      forAll(arbitrary[ControlDecision], nonEmptyString, arbitrary[LocalReferenceNumber]) {
        (controlDecision, eoriNumber, lrn) =>
          val setControlDecision = controlDecision.copy(principleEori = Some(eoriNumber))

          val doc: Document = renderDocument(
            "controlDecision.njk",
            Json.obj("controlDecisionMessage" -> Json.toJson(setControlDecision), "lrn" -> lrn.value)
          ).futureValue

          val getSummaryElements = doc.getElementsByClass("govuk-summary-list__row")

          getSummaryElements.size mustBe 4

          getSummaryElements.eq(0).select("dd").text mustBe setControlDecision.movementReferenceNumber
          getSummaryElements.eq(1).select("dd").text mustBe lrn.value
          getSummaryElements.eq(2).select("dd").text mustBe setControlDecision.principleEori.value
          getSummaryElements.eq(3).select("dd").text mustBe Format.controlDecisionDateFormatted(setControlDecision.dateOfControl)
      }
    }

    "must return trader name if eori number is unavailable" in {

      forAll(arbitrary[ControlDecision], arbitrary[LocalReferenceNumber]) {
        (controlDecision, lrn) =>
          val setControlDecision = controlDecision.copy(principleEori = None)

          val doc: Document = renderDocument(
            "controlDecision.njk",
            Json.obj("controlDecisionMessage" -> Json.toJson(setControlDecision), "lrn" -> lrn.value)
          ).futureValue

          val getSummaryElements = doc.getElementsByClass("govuk-summary-list__row")

          getSummaryElements.size mustBe 4

          getSummaryElements.eq(0).select("dd").text mustBe setControlDecision.movementReferenceNumber
          getSummaryElements.eq(1).select("dd").text mustBe lrn.value
          getSummaryElements.eq(2).select("dd").text mustBe setControlDecision.principleTraderName
          getSummaryElements.eq(3).select("dd").text mustBe Format.controlDecisionDateFormatted(setControlDecision.dateOfControl)
      }
    }
  }

}
