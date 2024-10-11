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

package controllers.departureP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.DepartureMovementP5Connector
import generators.Generators
import models.MessageStatus
import models.departureP5.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureP5MessageService

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllDeparturesP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val departureMovementP5Connector = mock[DepartureMovementP5Connector]
  private val departureP5MessageService    = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(departureMovementP5Connector)
    reset(departureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMovementP5Connector].toInstance(departureMovementP5Connector))
      .overrides(bind[DepartureP5MessageService].toInstance(departureP5MessageService))

  "ViewAllDeparturesP5Controller" - {

    "must return OK for a GET" in {
      val movement = DepartureMovement("id", Some("mrn"), "ref", LocalDateTime.now())
      when(departureMovementP5Connector.getAllMovementsForSearchQuery(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(DepartureMovements(Seq(movement), 1))))
      when(departureP5MessageService.getLatestMessagesForMovement(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Seq(
              OtherMovementAndMessage(
                "id",
                "ref",
                LocalDateTime.now(),
                LatestDepartureMessage(
                  DepartureMessage(
                    "messageId",
                    LocalDateTime.now(),
                    DepartureMessageType.DepartureNotification,
                    MessageStatus.Success
                  ),
                  "id"
                )
              )
            )
          )
        )

      val controllerUrl = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url

      val request = FakeRequest(GET, controllerUrl)

      val result = route(app, request).value

      status(result) mustEqual OK
    }
  }
}
