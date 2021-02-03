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

package models.departure

import com.lucidchart.open.xtract.XmlReader
import generators.Generators
import models.departure.ControlDecisionSpec.toXml
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq

class ControlDecisionSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with StreamlinedXmlEquality with OptionValues {

  "ControlDecision" - {

    "xml" - {
      "must deserialize" in {
        forAll(arbitrary[ControlDecision]) {
          controlDecision =>
            val expectedResult = XmlReader.of[ControlDecision].read(toXml(controlDecision)).toOption.value

            expectedResult mustBe controlDecision
        }
      }
    }
  }
}

object ControlDecisionSpec {

  def toXml(controlDecision: ControlDecision) =
    <CC060A>
      <HEAHEA>
        <DocNumHEA5>{controlDecision.movementReferenceNumber}</DocNumHEA5>
        <DatOfConNotHEA148>{controlDecision.dateOfControl}</DatOfConNotHEA148>
      </HEAHEA>
      <TRAPRIPC1>
        <NamPC17>{controlDecision.principleTraderName}</NamPC17>
        {
        controlDecision.principleEori.fold(NodeSeq.Empty) {
          principleEori => <TINPC159>{principleEori}</TINPC159>
        }
        }
      </TRAPRIPC1>
    </CC060A>

}
