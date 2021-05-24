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

package controllers.actions

import base.SpecBase
import connectors.BetaAuthorizationConnector
import models.EoriNumber
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import play.api.mvc.{Action, AnyContent, BodyParser, Request, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class PrivateBetaActionFilterSpec extends SpecBase {

  class Harness(authAction: PrivateBetaActionFilter) {

    private val fakeIdentifierAction = new IdentifierAction {
      override def parser: BodyParser[AnyContent] = stubBodyParser()

      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
        block(IdentifierRequest(request, "AB123456789"))

      override protected def executionContext: ExecutionContext = ExecutionContext.global
    }

    def onPageLoad(): Action[AnyContent] = (fakeIdentifierAction andThen authAction) {
      _ =>
        Results.Ok
    }
  }

  private val mockBetaAuthorizationConnector = mock[BetaAuthorizationConnector]

  override def beforeEach: Unit = {
    reset(
      mockBetaAuthorizationConnector
    )
    super.beforeEach
  }

  "PrivateBetaActionFilter" - {
    "render the page if user is a private beta user" in {
      val action = new PrivateBetaActionFilter(mockBetaAuthorizationConnector, ExecutionContext.global)

      when(mockBetaAuthorizationConnector.getBetaUser(eqTo(EoriNumber("AB123456789")))(any()))
        .thenReturn(Future.successful(true))

      val result = new Harness(action).onPageLoad()(IdentifierRequest(FakeRequest(), "AB123456789"))

      status(result) mustBe OK
    }
    "redirect to the old Interstitial controller if user is not a private beta user" in {
      val action = new PrivateBetaActionFilter(mockBetaAuthorizationConnector, ExecutionContext.global)

      when(mockBetaAuthorizationConnector.getBetaUser(eqTo(EoriNumber("AB123456789")))(any()))
        .thenReturn(Future.successful(false))

      val result = new Harness(action).onPageLoad()(IdentifierRequest(FakeRequest(), "AB123456789"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.OldServiceInterstitialController.onPageLoad().url)
    }
  }
}
