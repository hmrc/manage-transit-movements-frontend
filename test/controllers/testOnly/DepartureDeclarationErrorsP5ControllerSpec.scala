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

package controllers.testOnly

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.DepartureCacheConnector
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel.DepartureDeclarationErrorsP5ViewModelProvider
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel
import viewModels.sections.Section
import views.html.departure.TestOnly.DepartureDeclarationErrorsP5View

import scala.concurrent.Future

class DepartureDeclarationErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureDeclarationErrorsP5ViewModelProvider = mock[DepartureDeclarationErrorsP5ViewModelProvider]
  private val mockDepartureP5MessageService                     = mock[DepartureP5MessageService]
  private val mockCacheService: DepartureCacheConnector         = mock[DepartureCacheConnector]

  lazy val departureDeclarationErrorsController: String = controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureIdP5).url
  val sections: Seq[Section]                            = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureDeclarationErrorsP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureDeclarationErrorsP5ViewModelProvider].toInstance(mockDepartureDeclarationErrorsP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))

  "DepartureDeclarationErrorsP5Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockDepartureDeclarationErrorsP5ViewModelProvider.apply(any())(any(), any(), any()))
        .thenReturn(DepartureDeclarationErrorsP5ViewModel(lrn.toString))

      val departureDeclarationErrorsP5ViewModel = new DepartureDeclarationErrorsP5ViewModel(lrn.toString)

      val request = FakeRequest(GET, departureDeclarationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[DepartureDeclarationErrorsP5View]

      contentAsString(result) mustEqual
        view(departureDeclarationErrorsP5ViewModel)(request, messages, frontendAppConfig).toString
    }
  }
}
