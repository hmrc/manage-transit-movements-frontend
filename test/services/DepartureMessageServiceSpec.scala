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
import connectors.DeparturesMovementConnector
import generators.Generators
import models.XMLSubmissionNegativeAcknowledgementMessage
import models.departure.{ControlDecision, MessagesLocation, MessagesSummary, NoReleaseForTransitMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureMessageServiceSpec extends SpecBase with BeforeAndAfterEach with Matchers with Generators {

  private val mockDepartureConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockDepartureConnector)
  }

  val application = applicationBuilder()
    .overrides(bind[DeparturesMovementConnector].toInstance(mockDepartureConnector))
    .build()

  private val messageService = application.injector.instanceOf[DepartureMessageService]

  "DepartureMessageService" - {
    "getNoReleaseForTransitMessage" - {
      "must return NoReleaseForTransitMessage for the input departureId" in {
        val transitMessage = arbitrary[NoReleaseForTransitMessage].sample.value
        val messagesSummary =
          MessagesSummary(
            departureId,
            MessagesLocation(
              departureMessage           = s"/movements/departures/${departureId.index}/messages/3",
              guaranteeNotValid          = Some("/movements/departures/1234/messages/5"),
              declarationRejection       = Some("/movements/departures/1234/messages/7"),
              cancellationDecisionUpdate = Some("/movements/departures/1234/messages/9"),
              declarationCancellation    = Some("/movements/departures/1234/messages/11"),
              noReleaseForTransit        = Some("/movements/departures/1234/messages/12"),
              None
            )
          )

        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockDepartureConnector.getNoReleaseForTransitMessage(any())(any()))
          .thenReturn(Future.successful(Some(transitMessage)))

        messageService.noReleaseForTransitMessage(departureId).futureValue mustBe Some(transitMessage)
      }

      "must return None when getSummary fails to get noReleaseForTransit message" in {
        val messagesSummary =
          MessagesSummary(departureId, MessagesLocation(s"/movements/departures/${departureId.index}/messages/3", None, None, None, None, None, None))
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))

        messageService.noReleaseForTransitMessage(departureId).futureValue mustBe None
      }

      "must return None when getSummary call fails to get MessagesSummary" in {
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

        messageService.noReleaseForTransitMessage(departureId).futureValue mustBe None
      }
    }

    "getControlDecisionMessage" - {
      "must return ControlDecision for the input departureId" in {
        val transitMessage = arbitrary[ControlDecision].sample.value
        val messagesSummary =
          MessagesSummary(
            departureId,
            MessagesLocation(
              departureMessage = s"/movements/departures/${departureId.index}/messages/3",
              None,
              None,
              None,
              None,
              None,
              controlDecision = Some(s"/movements/departures/${departureId.index}/messages/5")
            )
          )

        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockDepartureConnector.getControlDecisionMessage(any())(any()))
          .thenReturn(Future.successful(Some(transitMessage)))

        messageService.controlDecisionMessage(departureId).futureValue.value mustBe transitMessage
      }

      "must return None when getSummary fails to get controlDecision message" in {
        val messagesSummary =
          MessagesSummary(departureId, MessagesLocation(s"/movements/departures/${departureId.index}/messages/3", None, None, None, None, None, None))
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))

        messageService.controlDecisionMessage(departureId).futureValue mustBe None
      }

      "must return None when getSummary call fails to get MessagesSummary" in {
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

        messageService.controlDecisionMessage(departureId).futureValue mustBe None
      }
    }

    "getXMLSubmissionNegativeAcknowledgementMessage" - {
      "must return XMLSubmissionNegativeAcknowledgementMessage for the input departureId" in {

        val xmlNegativeAcknowledgement = arbitrary[XMLSubmissionNegativeAcknowledgementMessage].sample.value
        val messagesSummary =
          MessagesSummary(
            departureId,
            MessagesLocation(s"/movements/departures/${departureId.index}/messages/3",
                             None,
                             None,
                             None,
                             None,
                             None,
                             None,
                             Some("/movements/departures/1234/messages/5"))
          )

        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockDepartureConnector.getXMLSubmissionNegativeAcknowledgementMessage(any())(any()))
          .thenReturn(Future.successful(Some(xmlNegativeAcknowledgement)))

        messageService.getXMLSubmissionNegativeAcknowledgementMessage(departureId).futureValue mustBe Some(xmlNegativeAcknowledgement)
      }

      "must return None when getSummary fails to get xml negative acknowledgement message" in {
        val messagesSummary =
          MessagesSummary(departureId, MessagesLocation(s"/movements/departures/${departureId.index}/messages/3", None, None, None, None, None, None))
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
        when(mockDepartureConnector.getXMLSubmissionNegativeAcknowledgementMessage(any())(any()))
          .thenReturn(Future.successful(None))

        messageService.getXMLSubmissionNegativeAcknowledgementMessage(departureId).futureValue mustBe None
      }

      "must return None when getSummary call fails to get MessagesSummary" in {
        when(mockDepartureConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

        messageService.getXMLSubmissionNegativeAcknowledgementMessage(departureId).futureValue mustBe None
      }
    }

  }
}
