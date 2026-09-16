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

package controllers.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC182CType
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DepartureMessageService, ReferenceDataService}
import viewModels.departure.IncidentViewModel.IncidentViewModelProvider
import viewModels.departure.IncidentViewModel
import viewModels.sections.Section
import views.html.departure.IncidentView

import scala.concurrent.Future

class IncidentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService      = mock[ReferenceDataService]
  private val mockIncidentViewModelProvider = mock[IncidentViewModelProvider]
  private val mockDepartureMessageService   = mock[DepartureMessageService]

  lazy val controller: String = controllers.departure.routes.IncidentController.onPageLoad(departureIdP5, incidentIndex, messageId).url
  private val sections        = arbitrary[Seq[Section]].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureMessageService)
    reset(mockIncidentViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[IncidentViewModelProvider].toInstance(mockIncidentViewModelProvider))
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  "IncidentController" - {

    val incidentsViewModel = new IncidentViewModel(lrn.toString, fakeCustomsOffice, isMultipleIncidents = true, sections, incidentIndex)

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC182CType]) {
        message =>
          when(mockDepartureMessageService.getMessage[CC182CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(departureReferenceNumbers))

          when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
            .thenReturn(Future.successful(fakeCustomsOffice))

          when(mockIncidentViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(incidentsViewModel))

          val request = FakeRequest(GET, controller)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[IncidentView]

          contentAsString(result) mustEqual
            view(incidentsViewModel, departureIdP5, messageId)(request, messages).toString
      }
    }

    "must redirect back to IncidentsDuringTransitController for a POST" in {
      val request = FakeRequest(POST, controller)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.IncidentsDuringTransitController.onPageLoad(departureIdP5, messageId).url
    }
  }

}
