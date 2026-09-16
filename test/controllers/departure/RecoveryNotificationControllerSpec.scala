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
import generated.CC035CType
import generators.Generators
import models.departure.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureMessageService
import viewModels.departure.RecoveryNotificationViewModel.RecoveryNotificationViewModelProvider
import viewModels.departure.RecoveryNotificationViewModel
import viewModels.sections.Section
import views.html.departure.RecoveryNotificationView

import scala.concurrent.Future

class RecoveryNotificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRecoveryNotificationViewModelProvider = mock[RecoveryNotificationViewModelProvider]
  private val mockDepartureMessageService               = mock[DepartureMessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRecoveryNotificationViewModelProvider)
    reset(mockDepartureMessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[RecoveryNotificationViewModelProvider].toInstance(mockRecoveryNotificationViewModelProvider))
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  private val sections                      = arbitrary[Seq[Section]].sample.value
  private val recoveryNotificationViewModel = new RecoveryNotificationViewModel(sections)

  private val routes = controllers.departure.routes.RecoveryNotificationController.onPageLoad(departureIdP5, messageId).url

  "RecoveryNotificationController Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC035CType]) {
        message =>
          when(mockDepartureMessageService.getMessage[CC035CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockRecoveryNotificationViewModelProvider.apply(any())(any())).thenReturn(recoveryNotificationViewModel)

          val request = FakeRequest(GET, routes)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[RecoveryNotificationView]

          contentAsString(result) mustEqual
            view(recoveryNotificationViewModel, lrn.value)(request, messages).toString
      }
    }
  }

}
