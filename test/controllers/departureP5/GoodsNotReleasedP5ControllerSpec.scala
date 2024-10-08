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
import generated.CC051CType
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel.GoodsNotReleasedP5ViewModelProvider
import viewModels.sections.Section
import views.html.departureP5.GoodsNotReleasedP5View

import scala.concurrent.Future

class GoodsNotReleasedP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGoodsNotReleasedP5ViewModelProvider = mock[GoodsNotReleasedP5ViewModelProvider]
  private val mockDepartureP5MessageService           = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGoodsNotReleasedP5ViewModelProvider)
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsNotReleasedP5ViewModelProvider].toInstance(mockGoodsNotReleasedP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val sections                    = arbitrary[Seq[Section]].sample.value
  private val goodsNotReleasedP5ViewModel = new GoodsNotReleasedP5ViewModel(sections, lrn.toString)

  private val routes = controllers.departureP5.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureIdP5, messageId).url

  "GoodsNotReleasedP5Controller Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC051CType]) {
        message =>
          when(mockDepartureP5MessageService.getMessage[CC051CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockGoodsNotReleasedP5ViewModelProvider.apply(any(), any())(any())).thenReturn(goodsNotReleasedP5ViewModel)

          val request = FakeRequest(GET, routes)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[GoodsNotReleasedP5View]

          contentAsString(result) mustEqual
            view(goodsNotReleasedP5ViewModel)(request, messages, frontendAppConfig).toString
      }
    }
  }

}
