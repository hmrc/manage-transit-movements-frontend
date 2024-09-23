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

package controllers.departureP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService

import scala.concurrent.Future

class IsDepartureCancelledP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  lazy val isDepartureCancelledRoute: String = routes.IsDepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, messageId).url

  "IsDepartureCancelledP5Controller" - {

    "must redirect to correct controller" - {
      "when decision is false or undefined" in {
        forAll(Gen.oneOf(None, Some(Number0))) {
          decision =>
            forAll(arbitrary[CC009CType].map {
              x =>
                x.copy(Invalidation = x.Invalidation.copy(decision = decision))
            }) {
              message =>
                when(mockDepartureP5MessageService.getMessage[CC009CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
                when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))

                val request = FakeRequest(GET, isDepartureCancelledRoute)

                val result = route(app, request).value

                status(result) mustEqual SEE_OTHER

                redirectLocation(result).value mustEqual
                  routes.DepartureNotCancelledP5Controller.onPageLoad(departureIdP5, messageId).url
            }
        }
      }

      "when decision is true" in {
        val decision = Some(Number1)
        forAll(arbitrary[CC009CType].map {
          x =>
            x.copy(Invalidation = x.Invalidation.copy(decision = decision))
        }) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC009CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))

            val request = FakeRequest(GET, isDepartureCancelledRoute)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.DepartureCancelledP5Controller.onPageLoad(departureIdP5, messageId).url
        }
      }
    }
  }

}
