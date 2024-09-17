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
import models.LockCheck
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.Helpers._
import services.DraftDepartureService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LockActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  class Harness(authAction: LockActionProvider) {

    def onPageLoad(): Action[AnyContent] = (stubControllerComponents().actionBuilder andThen FakeIdentifierAction(true) andThen authAction(lrn.toString)) {
      _ =>
        Results.Ok
    }

  }

  val service: DraftDepartureService = mock[DraftDepartureService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DraftDepartureService].toInstance(service))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(service)
  }

  "Lock Action" - {

    "must return Ok when lock is open" in {

      when(service.checkLock(any())(any())).thenReturn(Future(LockCheck.Unlocked))

      val actionProvider: LockActionProvider = app.injector.instanceOf[LockActionProvider]

      val controller = new Harness(actionProvider)
      val result     = controller.onPageLoad()(fakeRequest)

      status(result) `mustBe` OK
    }

    "must redirect to lock page when lock is not open" in {

      when(service.checkLock(any())(any())).thenReturn(Future(LockCheck.Locked))

      val actionProvider: LockActionProvider = app.injector.instanceOf[LockActionProvider]

      val controller = new Harness(actionProvider)
      val result     = controller.onPageLoad()(fakeRequest)

      status(result) `mustBe` SEE_OTHER
      redirectLocation(result).value `mustBe` controllers.departureP5.drafts.routes.DraftLockedController.onPageLoad().url
    }

    "must redirect to technical difficulties when lock check fails" in {

      when(service.checkLock(any())(any())).thenReturn(Future(LockCheck.LockCheckFailure))

      val actionProvider: LockActionProvider = app.injector.instanceOf[LockActionProvider]

      val controller = new Harness(actionProvider)
      val result     = controller.onPageLoad()(fakeRequest)

      status(result) `mustBe` SEE_OTHER
      redirectLocation(result).value `mustBe` controllers.routes.ErrorController.technicalDifficulties().url
    }
  }

}
