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
import generated.CC055CType
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel.GuaranteeRejectedNotAmendableP5ViewModelProvider
import views.html.departureP5.GuaranteeRejectedNotAmendableP5View

import scala.concurrent.Future

class GuaranteeRejectedNotAmendableP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService                        = mock[DepartureP5MessageService]
  private val mockGuaranteeRejectedNotAmendableP5ViewModelProvider = mock[GuaranteeRejectedNotAmendableP5ViewModelProvider]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GuaranteeRejectedNotAmendableP5ViewModelProvider].toInstance(mockGuaranteeRejectedNotAmendableP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  "GuaranteeRejectedNotAmendable" - {

    lazy val controller = routes.GuaranteeRejectedNotAmendableP5Controller.onPageLoad(departureIdP5, messageId).url

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        forAll(arbitrary[CC055CType], arbitrary[GuaranteeRejectedNotAmendableP5ViewModel]) {
          (message, viewModel) =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

            when(mockGuaranteeRejectedNotAmendableP5ViewModelProvider.apply(any(), any(), any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(viewModel))

            val request = FakeRequest(GET, controller)

            val result = route(app, request).value

            status(result) mustEqual OK

            val view = injector.instanceOf[GuaranteeRejectedNotAmendableP5View]

            contentAsString(result) mustEqual
              view(viewModel, departureIdP5, messageId)(request, messages).toString
        }
      }
    }
  }
}
