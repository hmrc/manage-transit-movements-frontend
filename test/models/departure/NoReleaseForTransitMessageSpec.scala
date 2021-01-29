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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import utils.Format

import scala.xml.NodeSeq

class NoReleaseForTransitMessageSpec extends AnyFreeSpec with Matchers with Generators with OptionValues {

  "NoReleaseForTransitMessage" - {
    "must read xml" in {
      val expectedResult: NoReleaseForTransitMessage = arbitrary[NoReleaseForTransitMessage].sample.value
      val validXml: NodeSeq                          = <CC051B>
        <HEAHEA>
          <DocNumHEA5>{expectedResult.mrn}</DocNumHEA5>
          {expectedResult.noReleaseMotivation.fold(NodeSeq.Empty) {
          noReleaseMotivation =>
            <NoRelMotHEA272>{noReleaseMotivation}</NoRelMotHEA272>}}
          <TotNumOfIteHEA305>{expectedResult.totalNumberOfItems}</TotNumOfIteHEA305>
        </HEAHEA>
        <CUSOFFDEPEPT><RefNumEPT1>{expectedResult.officeOfDepartureRefNumber}</RefNumEPT1></CUSOFFDEPEPT>
        <CONRESERS>
          <ConResCodERS16>{expectedResult.controlResult.code}</ConResCodERS16>
          <ConDatERS14>{Format.dateFormatted(expectedResult.controlResult.datLimERS69)}</ConDatERS14>
        </CONRESERS>
        {expectedResult.resultsOfControl.fold(NodeSeq.Empty) {
          resultsOfControl =>
            resultsOfControl.map { rc =>
              <RESOFCON534><ConInd424>{rc.controlIndicator}</ConInd424>
                {rc.description.fold(NodeSeq.Empty) {
                description => <DesTOC2>{description}</DesTOC2>
              }}
              </RESOFCON534>
            }
        }}

      </CC051B>

      XmlReader.of[NoReleaseForTransitMessage].read(validXml).toOption mustBe Some(expectedResult)

    }
  }
}
