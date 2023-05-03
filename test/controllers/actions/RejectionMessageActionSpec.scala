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
import connectors.DepartureCacheConnector
import controllers.routes
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
import services.{DepartureP5MessageService, ReferenceDataService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RejectionMessageActionSpec extends SpecBase with BeforeAndAfterEach with AppWithDefaultMockFixtures {

  val mockMessageService: DepartureP5MessageService  = mock[DepartureP5MessageService]
  val mockCacheService: DepartureCacheConnector      = mock[DepartureCacheConnector]
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  val functionalError1: FunctionalError = FunctionalError("1", "12", "Codelist violation", None)
  val functionalError2: FunctionalError = FunctionalError("2", "14", "Rule violation", None)

  val message: IE056Data = IE056Data(
    IE056MessageData(
      TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
      Seq(functionalError1, functionalError2)
    )
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockMessageService)
    Mockito.reset(mockCacheService)
  }

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "RejectionMessageAction" - {
    "must return 200 when an unloading permission is available" in {

      when(mockMessageService.getRejectionMessage(any())(any(), any())).thenReturn(Future.successful(Some(message)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))

      val rejectionMessageProvider = (new RejectionMessageActionProvider(mockMessageService, mockCacheService)(implicitly))(departureIdP5)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = rejectionMessageProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual OK
    }

    "must return 303 and redirect to technical difficulties when unavailable" in {

      when(mockMessageService.getRejectionMessage(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))

      val rejectionMessageProvider = (new RejectionMessageActionProvider(mockMessageService, mockCacheService)(implicitly))(departureIdP5)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = rejectionMessageProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.ErrorController.technicalDifficulties().url
    }
  }
}
