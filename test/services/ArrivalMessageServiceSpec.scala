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

package services

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import models.ArrivalId
import models.arrival.{MessagesLocation, MessagesSummary, XMLSubmissionNegativeAcknowledgementMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalMessageServiceSpec extends SpecBase with Generators {

  val mockConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val arrivalMessageService = new ArrivalMessageService(mockConnector)

  private val arrivalId = ArrivalId(1)

  "ArrivalMessageService" - {
    "must return XMLSubmissionNegativeAcknowledgementMessage for the input arrivalId" in {

      val xmlNegativeAcknowledgement = arbitrary[XMLSubmissionNegativeAcknowledgementMessage].sample.value
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", None, Some("/movements/arrivals/1234/messages/5")))

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getXMLSubmissionNegativeAcknowledgementMessage(any())(any()))
        .thenReturn(Future.successful(Some(xmlNegativeAcknowledgement)))

      arrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(arrivalId).futureValue mustBe Some(xmlNegativeAcknowledgement)
    }

    "must return None when getSummary fails to get xml negative acknowledgement message" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", None))
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getXMLSubmissionNegativeAcknowledgementMessage(any())(any()))
        .thenReturn(Future.successful(None))

      arrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(arrivalId).futureValue mustBe None
    }

    "must return None when getSummary call fails to get MessagesSummary" in {
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      arrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(arrivalId).futureValue mustBe None
    }
  }
}
