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

package helper

import base.SpecBase
import generators.Generators
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import generated.CC182CType
import utils.IncidentsDuringTransitP5Helper
import viewModels.sections.Section.StaticSection

class IncidentsDuringTransitP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "ConsignmentAnswersHelper" - {

    val CC182CType = Arbitrary.arbitrary[CC182CType].sample.value

    "rows" - {
      "mrnRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation.copy(MRN = value))

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType)
              val result = helper.mrnRow.value

              result.key.value mustBe "Movement Reference Number (MRN)"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }

    "sections" - {
      "incidentInformationSection" - {
        "must return a static section" in {
          val helper = new IncidentsDuringTransitP5Helper(CC182CType)
          val result = helper.incidentInformationSection

          result mustBe a[StaticSection]
          result.rows.size mustBe 1
        }
      }
    }

  }
}
