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
import viewModels.P5.departure.RecoveryNotificationViewModel
import viewModels.P5.departure.RecoveryNotificationViewModel.RecoveryNotificationViewModelProvider
import viewModels.sections.Section
import views.html.departureP5.RecoveryNotificationView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class RecoveryNotificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRecoveryNotificationViewModelProvider = mock[RecoveryNotificationViewModelProvider]
  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRecoveryNotificationViewModelProvider)
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[RecoveryNotificationViewModelProvider].toInstance(mockRecoveryNotificationViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val message: IE035Data = IE035Data(
    IE035MessageData(
      TransitOperationIE035(mrn, LocalDate.parse("2014-06-09", DateTimeFormatter.ISO_DATE)),
      RecoveryNotification(LocalDate.parse("2014-06-09", DateTimeFormatter.ISO_DATE), "text", "1000", "EUR")
    )
  )

  private val sections                      = arbitrary[Seq[Section]].sample.value
  private val recoveryNotificationViewModel = new RecoveryNotificationViewModel(sections)

  private val routes = controllers.departureP5.routes.RecoveryNotificationController.onPageLoad(departureIdP5, messageId).url

  "RecoveryNotificationController Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockDepartureP5MessageService.getMessageWithMessageId[IE035Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))
      when(mockRecoveryNotificationViewModelProvider.apply(any())(any())).thenReturn(recoveryNotificationViewModel)

      val request = FakeRequest(GET, routes)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RecoveryNotificationView]

      contentAsString(result) mustEqual
        view(recoveryNotificationViewModel, lrn)(request, messages).toString
    }

  }
}
