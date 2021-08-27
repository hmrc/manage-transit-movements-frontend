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

package models.arrival

import base.SpecBase
import com.lucidchart.open.xtract.XmlReader
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.xml.{Elem, NodeSeq}

class XMLSubmissionNegativeAcknowledgementMessageSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with Generators {

  "XMLSubmissionNegativeAcknowledgementMessage" - {
    "must deserialize from XML" in {
      forAll(arbitrary[XMLSubmissionNegativeAcknowledgementMessage]) {
        rejectionMessage =>
          val xml: Elem =
            <CC917A>
              <HEAHEA>
                {
              rejectionMessage.movementReferenceNumber.fold(NodeSeq.Empty) {
                mrn =>
                  <DocNumHEA5>{mrn}</DocNumHEA5>
              }
            }
                {
              rejectionMessage.localReferenceNumber.fold(NodeSeq.Empty) {
                lrn =>
                  <RefNumHEA4>{lrn}</RefNumHEA4>
              }
            }
              </HEAHEA>
              <FUNERRER1>
                <ErrTypER11>{rejectionMessage.error.errorType.code}</ErrTypER11>
                <ErrPoiER12>{rejectionMessage.error.pointer.value}</ErrPoiER12>
                {
              rejectionMessage.error.reason.fold(NodeSeq.Empty) {
                reason =>
                  <ErrReaER13>{reason}</ErrReaER13>
              }
            }
                {
              rejectionMessage.error.originalAttributeValue.fold(NodeSeq.Empty) {
                attrValue =>
                  <OriAttValER14>{attrValue}</OriAttValER14>
              }
            }
              </FUNERRER1>
            </CC917A>

          val result = XmlReader.of[XMLSubmissionNegativeAcknowledgementMessage].read(xml)

          result.toOption.value mustEqual rejectionMessage
      }
    }
  }
}
