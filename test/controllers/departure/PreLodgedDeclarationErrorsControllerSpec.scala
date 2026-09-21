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

package controllers.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC056CType
import generators.Generators
import models.departure.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureMessageService
import views.html.departure.PreLodgedDeclarationErrorsView

import scala.concurrent.Future

class PreLodgedDeclarationErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService = mock[DepartureMessageService]

  lazy val controllerRoute: String =
    routes.PreLodgedDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  "PreLodgedDeclarationErrorsController" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC056CType]) {
        message =>
          beforeEach()

          when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          val request = FakeRequest(GET, controllerRoute)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[PreLodgedDeclarationErrorsView]

          contentAsString(result) mustEqual
            view(departureIdP5, lrn.value)(request, messages).toString

          verify(mockDepartureMessageService).getMessage[CC056CType](eqTo(departureIdP5), eqTo(messageId))(any(), any(), any())
      }
    }
  }
}
