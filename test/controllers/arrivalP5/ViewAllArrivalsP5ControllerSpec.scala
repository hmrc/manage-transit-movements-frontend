/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.arrivalP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ArrivalMovementP5Connector
import generators.Generators
import models.MessageStatus
import models.arrivalP5.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ArrivalP5MessageService

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllArrivalsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val arrivalMovementP5Connector = mock[ArrivalMovementP5Connector]
  private val arrivalP5MessageService    = mock[ArrivalP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(arrivalMovementP5Connector)
    reset(arrivalP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalMovementP5Connector].toInstance(arrivalMovementP5Connector))
      .overrides(bind[ArrivalP5MessageService].toInstance(arrivalP5MessageService))

  "ViewAllArrivalsP5Controller" - {

    "must return OK for a GET" in {
      val arrivalMovement = ArrivalMovement("arrivialId", "mrn", LocalDateTime.now())
      when(arrivalMovementP5Connector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(ArrivalMovements(Seq(arrivalMovement), 1))))
      when(arrivalP5MessageService.getLatestMessagesForMovements(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Seq(
              OtherMovementAndMessage(
                arrivalMovement,
                LatestArrivalMessage(
                  ArrivalMessage(
                    "messageId",
                    LocalDateTime.now(),
                    ArrivalMessageType.ArrivalNotification,
                    MessageStatus.Success
                  ),
                  "id"
                )
              )
            )
          )
        )

      val controllerUrl = controllers.arrivalP5.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      status(result) mustEqual OK
    }
  }
}
