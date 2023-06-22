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

package controllers

import base.SpecBase
import generators.Generators
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.WhatDoYouWantToDoService
import views.html.WhatDoYouWantToDoView

import scala.concurrent.Future

class WhatDoYouWantToDoControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockWhatDoYouWantToDoService: WhatDoYouWantToDoService = mock[WhatDoYouWantToDoService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockWhatDoYouWantToDoService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[WhatDoYouWantToDoService].toInstance(mockWhatDoYouWantToDoService)
      )

  "WhatDoYouWantToDo Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(
        arbitrary[Availability],
        arbitrary[Availability],
        Gen.option(arbitrary[DraftAvailability]),
        Gen.alphaNumStr,
        Gen.alphaNumStr
      ) {
        (arrivalsAvailability, departuresAvailability, draftDeparturesAvailability, viewAllArrivalsUrl, viewAllDeparturesUrl) =>
          beforeEach()

          when(mockWhatDoYouWantToDoService.fetchArrivalsAvailability()(any(), any()))
            .thenReturn(Future.successful(arrivalsAvailability))

          when(mockWhatDoYouWantToDoService.fetchArrivalsUrl())
            .thenReturn(viewAllArrivalsUrl)

          when(mockWhatDoYouWantToDoService.fetchDeparturesAvailability()(any()))
            .thenReturn(Future.successful(departuresAvailability))

          when(mockWhatDoYouWantToDoService.fetchDeparturesUrl())
            .thenReturn(viewAllDeparturesUrl)

          when(mockWhatDoYouWantToDoService.fetchDraftDepartureAvailability()(any(), any()))
            .thenReturn(Future.successful(draftDeparturesAvailability))

          val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
          val result  = route(app, request).value

          val view = injector.instanceOf[WhatDoYouWantToDoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(arrivalsAvailability, departuresAvailability, draftDeparturesAvailability, viewAllArrivalsUrl, viewAllDeparturesUrl)(
              request,
              messages
            ).toString

          verify(mockWhatDoYouWantToDoService).fetchDraftDepartureAvailability()(any(), any())
      }
    }
  }
}
