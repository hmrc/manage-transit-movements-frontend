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
import connectors.DeparturesDraftsP5Connector
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, Request, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LockActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  val dataRequest: IdentifierRequest[AnyContent] = IdentifierRequest(FakeRequest(GET, "/").asInstanceOf[Request[AnyContent]], "id")

  class Harness(authAction: LockActionProvider) {

    def onPageLoad(): Action[AnyContent] = (stubControllerComponents().actionBuilder andThen FakeIdentifierAction.apply() andThen authAction(lrn.toString)) {
      _ =>
        Results.Ok
    }
  }

  val connector: DeparturesDraftsP5Connector = mock[DeparturesDraftsP5Connector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DeparturesDraftsP5Connector].toInstance(connector))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
  }

  "Lock Action" - {

    "must return Ok when lock is open" in {

      when(connector.checkLock(any())(any())).thenReturn(Future(true))

      val actionProvider: LockActionProvider = app.injector.instanceOf[LockActionProvider]

      val controller = new Harness(actionProvider)
      val result     = controller.onPageLoad()(fakeRequest)

      status(result) mustBe OK
    }

    "must redirect to lock page when lock is not open" in {

      when(connector.checkLock(any())(any())).thenReturn(Future(false))

      val actionProvider: LockActionProvider = app.injector.instanceOf[LockActionProvider]

      val controller = new Harness(actionProvider)
      val result     = controller.onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.departure.drafts.routes.DraftLockedController.onPageLoad().url
    }
  }

}
