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

package models.departure

import com.lucidchart.open.xtract.XmlReader
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq

class ResultsOfControlSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with StreamlinedXmlEquality with OptionValues {

  "ResultsOfControl" - {

    "must serialize ResultsOfControl to xml" in {
      forAll(arbitrary[ResultsOfControl]) {
        resultsOfControl =>
          val xml =
            <RESOFCON534>
              <ConInd424>{resultsOfControl.controlIndicator}</ConInd424>
              {
              resultsOfControl.description.fold(NodeSeq.Empty) {
                description => <DesTOC2>{description}</DesTOC2>
              }
            }
            </RESOFCON534>
          val result = XmlReader.of[ResultsOfControl].read(xml).toOption.value
          result mustBe resultsOfControl
      }
    }
  }
}
