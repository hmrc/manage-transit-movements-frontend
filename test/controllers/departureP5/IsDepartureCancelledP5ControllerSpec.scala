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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService

import java.time.LocalDateTime
import scala.concurrent.Future

class IsDepartureCancelledP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val customsReferenceNumber = Gen.alphaNumStr.sample.value

  lazy val isDepartureCancelledRoute: String = routes.IsDepartureCancelledP5Controller.isDeclarationCancelled(departureIdP5, messageId).url

  "IsDepartureCancelledP5Controller" - {

    "must redirect to correct controller" - {
      "when decision is true" in {
        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "0",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))

        val request = FakeRequest(GET, isDepartureCancelledRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.DepartureNotCancelledP5Controller.onPageLoad(departureIdP5, messageId).url
      }

      "when decision is false" in {
        val message: IE009Data = IE009Data(
          IE009MessageData(
            TransitOperationIE009(
              Some("abd123")
            ),
            Invalidation(
              Some(LocalDateTime.now()),
              "1",
              "1",
              Some("some justification")
            ),
            CustomsOfficeOfDeparture(
              s"$customsReferenceNumber"
            )
          )
        )

        when(mockDepartureP5MessageService.getMessageWithMessageId[IE009Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
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
