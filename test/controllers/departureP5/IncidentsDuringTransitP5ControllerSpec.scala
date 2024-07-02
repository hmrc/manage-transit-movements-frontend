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
import generated.CC182CType
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel.IncidentsDuringTransitP5ViewModelProvider
import views.html.departureP5.IncidentsDuringTransitP5View

import scala.concurrent.Future

class IncidentsDuringTransitP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService                      = mock[ReferenceDataService]
  private val mockIncidentsDuringTransitP5ViewModelProvider = mock[IncidentsDuringTransitP5ViewModelProvider]
  private val mockDepartureP5MessageService                 = mock[DepartureP5MessageService]

  lazy val controller: String        = controllers.departureP5.routes.IncidentsDuringTransitP5Controller.onPageLoad(departureIdP5, messageId).url
  private val customsReferenceNumber = Gen.alphaNumStr.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
    reset(mockIncidentsDuringTransitP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[IncidentsDuringTransitP5ViewModelProvider].toInstance(mockIncidentsDuringTransitP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  "IncidentsDuringTransitP5Controller" - {

    val incidentsViewModel = new IncidentsDuringTransitP5ViewModel(lrn.toString, Left(customsReferenceNumber), isMultipleIncidents = true)

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC182CType]) {
        message =>
          when(mockDepartureP5MessageService.getMessage[CC182CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(departureReferenceNumbers))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left(customsReferenceNumber)))
          when(mockIncidentsDuringTransitP5ViewModelProvider.apply(any(), any(), any()))
            .thenReturn(incidentsViewModel)

          val request = FakeRequest(GET, controller)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[IncidentsDuringTransitP5View]

          contentAsString(result) mustEqual
            view(incidentsViewModel)(request, messages).toString
      }
    }
  }
}
