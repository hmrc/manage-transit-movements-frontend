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

package services

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import models.ArrivalId
import models.arrival.{MessagesLocation, MessagesSummary, XMLSubmissionNegativeAcknowledgementMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
class ArrivalMessageServiceSpec extends SpecBase with BeforeAndAfterEach with Matchers with Generators  {

  val mockConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }
  val application = applicationBuilder()
    .overrides(bind[ArrivalMovementConnector].toInstance(mockConnector))
    .build()

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val arrivalId = ArrivalId(1)

  "ArrivalNotificationMessageService" - {
    "must return ArrivalMovementRequest model for the input arrivalId" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))
      val arrivalMovementRequest: XMLSubmissionNegativeAcknowledgementMessage = arbitrary[XMLSubmissionNegativeAcknowledgementMessage].sample.value

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getXMLSubmissionNegativeAcknowledgementMessage(any())(any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      setExistingUserAnswers(emptyUserAnswers)

      val arrivalMovementMessageService = app.injector.instanceOf[XMLSubmissionNegativeAcknowledgementMessage]

      arrivalMessageService.(arrivalId).futureValue mustBe Some(arrivalMovementRequest)

    }

    "must return None when getSummary call fails to get MessagesSummary" in {
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      setExistingUserAnswers(emptyUserAnswers)

      val arrivalRejectionService = app.injector.instanceOf[ArrivalRejectionService]

      arrivalRejectionService.arrivalRejectionMessage(arrivalId).futureValue mustBe None
    }
  }

}
