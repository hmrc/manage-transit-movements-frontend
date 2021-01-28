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
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import scala.xml.NodeSeq

class NoReleaseForTransitMessageSpec extends AnyFreeSpec with Matchers with Generators with OptionValues {

  "NoReleaseForTransitMessage" - {
    "must read xml" in {
      val expectedResult: NoReleaseForTransitMessage = NoReleaseForTransitMessage("mrn", Some("ref"), 111, "token")
      val validXml: NodeSeq                          = <CC051B>
        <HEAHEA>
          <DocNumHEA5>{expectedResult.mrn}</DocNumHEA5>
          {expectedResult.noReleaseMotivation.fold(NodeSeq.Empty) {
          noReleaseMotivation =>
            <NoRelMotHEA272>{noReleaseMotivation}</NoRelMotHEA272>}}
          <TotNumOfIteHEA305>{expectedResult.totalNumberOfItems}</TotNumOfIteHEA305>
        </HEAHEA>
        <CUSOFFDEPEPT><RefNumEPT1>{expectedResult.officeOfDepartureRefNumber}</RefNumEPT1></CUSOFFDEPEPT>
      </CC051B>

      XmlReader.of[NoReleaseForTransitMessage].read(validXml).toOption mustBe Some(expectedResult)

    }
  }
}
