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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import models.arrivalP5.{CustomsOfficeOfDestinationActual, IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ArrivalP5MessageService, ReferenceDataService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalRejectionMessageActionSpec extends SpecBase with BeforeAndAfterEach with AppWithDefaultMockFixtures {

  val mockMessageService: ArrivalP5MessageService    = mock[ArrivalP5MessageService]
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  val functionalError1: FunctionalError = FunctionalError("1", "12", "Codelist violation", None)
  val functionalError2: FunctionalError = FunctionalError("2", "14", "Rule violation", None)

  val message: IE057Data = IE057Data(
    IE057MessageData(
      TransitOperationIE057("MRNCD3232"),
      CustomsOfficeOfDestinationActual("1234"),
      Seq(functionalError1, functionalError2)
    )
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockMessageService)
  }

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "Arrival RejectionMessageAction" - {
    "must return 200 when rejection from office of destination is available" in {

      when(mockMessageService.getMessageWithMessageId[IE057Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))

      val rejectionMessageProvider = (new ArrivalRejectionMessageActionProvider(mockMessageService)(implicitly))(arrivalIdP5, messageId)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = rejectionMessageProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual OK
    }

    "must return 303 and redirect to technical difficulties when unavailable" in {

      when(mockMessageService.getMessageWithMessageId[IE057Data](any(), any())(any(), any(), any())).thenReturn(Future.failed(new Throwable()))

      val rejectionMessageProvider = (new ArrivalRejectionMessageActionProvider(mockMessageService)(implicitly))(arrivalIdP5, messageId)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = rejectionMessageProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.ErrorController.technicalDifficulties().url
    }
  }
}
