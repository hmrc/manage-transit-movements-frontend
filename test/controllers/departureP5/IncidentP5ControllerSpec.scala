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
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.IncidentP5ViewModel
import viewModels.P5.departure.IncidentP5ViewModel.IncidentP5ViewModelProvider
import viewModels.sections.Section
import views.html.departureP5.IncidentP5View

import scala.concurrent.Future

class IncidentP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService        = mock[ReferenceDataService]
  private val mockIncidentP5ViewModelProvider = mock[IncidentP5ViewModelProvider]
  private val mockDepartureP5MessageService   = mock[DepartureP5MessageService]

  lazy val controller: String = controllers.departureP5.routes.IncidentP5Controller.onPageLoad(departureIdP5, incidentIndex, messageId).url
  private val sections        = arbitrary[Seq[Section]].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
    reset(mockIncidentP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[IncidentP5ViewModelProvider].toInstance(mockIncidentP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  "IncidentP5Controller" - {

    val incidentsViewModel = new IncidentP5ViewModel(lrn.toString, fakeCustomsOffice, isMultipleIncidents = true, sections, incidentIndex)

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC182CType]) {
        message =>
          when(mockDepartureP5MessageService.getMessage[CC182CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(departureReferenceNumbers))

          when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
            .thenReturn(Future.successful(fakeCustomsOffice))

          when(mockIncidentP5ViewModelProvider.apply(any(), any(), any(), any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(incidentsViewModel))

          val request = FakeRequest(GET, controller)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[IncidentP5View]

          contentAsString(result) mustEqual
            view(incidentsViewModel, departureIdP5, messageId)(request, messages).toString
      }
    }

    "must redirect back to IncidentsDuringTransitP5Controller for a POST" in {
      val request = FakeRequest(POST, controller)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.IncidentsDuringTransitP5Controller.onPageLoad(departureIdP5, messageId).url
    }
  }

}
