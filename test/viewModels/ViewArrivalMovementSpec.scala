/*
 * Copyright 2020 HM Revenue & Customs
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

package viewModels

import base.SpecBase
import generators.ModelGenerators
import models.Movement
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewArrivalMovementSpec extends SpecBase with MustMatchers with ModelGenerators with ScalaCheckPropertyChecks with NunjucksSupport {


  "viewArrivalMovement" - {
    "must serialise" in {

      forAll(arbitrary[ViewArrivalMovement]) {
        viewArrivalMovement =>
          Json.toJson(viewArrivalMovement) mustEqual buildJson(viewArrivalMovement)
      }
    }

    "must deserialise" in {

      forAll(arbitrary[ViewArrivalMovement]) {
        viewArrivalMovement =>
          buildJson(viewArrivalMovement).as[ViewArrivalMovement] mustBe viewArrivalMovement
      }
    }

    "must serialise and deserialise" in {
      forAll(arbitrary[ViewArrivalMovement]){
        viewArrivalMovement =>
          Json.toJson(viewArrivalMovement).as[ViewArrivalMovement] mustBe viewArrivalMovement
      }
    }
  }

  private def buildJson(viewArrivalMovement: ViewArrivalMovement): JsObject = {

    viewArrivalMovement.tables.map {
      case (date, rows) =>
        Json.obj(
          date -> buildRows(rows)
        )
    }.fold(JsObject.empty)(_ ++ _)
  }

  private def buildRows(rows: Seq[Movement]): Seq[JsObject] = {
    rows.map {
      row =>
        Json.obj(
          "updated" -> row.updated,
          "mrn" -> row.mrn,
          "traderName" -> row.traderName,
          "office" -> row.office,
          "procedure" -> row.procedure,
          "status" -> row.status,
          "actions" -> row.actions
        )
    }
  }
}
