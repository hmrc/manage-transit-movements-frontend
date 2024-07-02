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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.{Action, AnyContent, Results}
import play.api.test.Helpers._

class P5SwitchActionSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  private class Harness(actionProvider: P5SwitchActionProvider, isOnLegacyEnrolment: Boolean) {

    def onPageLoad(): Action[AnyContent] = (
      stubControllerComponents().actionBuilder andThen
        FakeIdentifierAction.apply(isOnLegacyEnrolment) andThen
        actionProvider()
    ) {
      _ => Results.Ok
    }
  }

  "P5 Enabled Action" - {

    "when P5 is enabled" - {
      "and user is on latest enrolment" - {
        "must return Ok" in {
          val app = super
            .guiceApplicationBuilder()
            .configure("microservice.services.features.isPhase5Enabled" -> true)
            .build()

          running(app) {
            val actionProvider: P5SwitchActionProvider = app.injector.instanceOf[P5SwitchActionProvider]

            val controller = new Harness(actionProvider, isOnLegacyEnrolment = false)
            val result     = controller.onPageLoad()(fakeRequest)

            status(result) mustBe OK
          }
        }
      }

      "and user is on legacy enrolment" - {
        "must redirect to enrolment guidance page" in {
          val app = super
            .guiceApplicationBuilder()
            .configure("microservice.services.features.isPhase5Enabled" -> true)
            .build()

          running(app) {
            val actionProvider: P5SwitchActionProvider = app.injector.instanceOf[P5SwitchActionProvider]

            val controller = new Harness(actionProvider, isOnLegacyEnrolment = true)
            val result     = controller.onPageLoad()(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe frontendAppConfig.enrolmentGuidancePage
          }
        }
      }
    }

    "when P5 is disabled" - {
      "must redirect to not found" in {
        forAll(arbitrary[Boolean]) {
          isOnLegacyEnrolment =>
            val app = super
              .guiceApplicationBuilder()
              .configure("microservice.services.features.isPhase5Enabled" -> false)
              .build()

            running(app) {
              val actionProvider: P5SwitchActionProvider = app.injector.instanceOf[P5SwitchActionProvider]

              val controller = new Harness(actionProvider, isOnLegacyEnrolment)
              val result     = controller.onPageLoad()(fakeRequest)

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustBe controllers.routes.ErrorController.notFound().url
            }
        }
      }
    }
  }
}
