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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AmendmentService, DepartureP5MessageService}

import scala.concurrent.Future

class AmendmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]
  private val mockAmendmentService          = mock[AmendmentService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockAmendmentService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[AmendmentService].toInstance(mockAmendmentService))

  "AmendmentController" - {

    lazy val prepareForAmendmentRoute = routes.AmendmentController.prepareForAmendment(departureIdP5).url

    "prepareForAmendment" - {

      "must redirect to task list on success" in {
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(eqTo(departureIdP5))(any(), any()))
          .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

        when(mockAmendmentService.prepareForAmendment(eqTo(lrn.value), eqTo(departureIdP5))(any()))
          .thenReturn(Future.successful(httpResponse(OK)))

        val request = FakeRequest(GET, prepareForAmendmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value `mustBe` frontendAppConfig.departureFrontendTaskListUrl(lrn.value)
      }

      "must redirect to tech difficulties on failure" in {
        when(mockDepartureP5MessageService.getDepartureReferenceNumbers(eqTo(departureIdP5))(any(), any()))
          .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

        when(mockAmendmentService.prepareForAmendment(eqTo(lrn.value), eqTo(departureIdP5))(any()))
          .thenReturn(Future.successful(httpResponse(INTERNAL_SERVER_ERROR)))

        val request = FakeRequest(GET, prepareForAmendmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value `mustBe` controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }

}
