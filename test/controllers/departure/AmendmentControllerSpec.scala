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
import generators.Generators
import models.departure.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{AmendmentService, DepartureMessageService}

import scala.concurrent.Future

class AmendmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService = mock[DepartureMessageService]
  private val mockAmendmentService        = mock[AmendmentService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockAmendmentService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))
      .overrides(bind[AmendmentService].toInstance(mockAmendmentService))

  "AmendmentController" - {

    lazy val prepareForAmendmentRoute = routes.AmendmentController.prepareForAmendment(departureIdP5).url

    "prepareForAmendment" - {

      "must redirect to task list on success" in {
        when(mockDepartureMessageService.getDepartureReferenceNumbers(eqTo(departureIdP5))(any(), any()))
          .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

        when(mockAmendmentService.prepareForAmendment(eqTo(lrn.value), eqTo(departureIdP5))(any()))
          .thenReturn(Future.successful(httpResponse(OK)))

        val request = FakeRequest(GET, prepareForAmendmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.departureFrontendTaskListUrl(lrn.value)
      }

      "must redirect to tech difficulties on failure" in {
        when(mockDepartureMessageService.getDepartureReferenceNumbers(eqTo(departureIdP5))(any(), any()))
          .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

        when(mockAmendmentService.prepareForAmendment(eqTo(lrn.value), eqTo(departureIdP5))(any()))
          .thenReturn(Future.successful(httpResponse(INTERNAL_SERVER_ERROR)))

        val request = FakeRequest(GET, prepareForAmendmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }

}
