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
import views.html.departure.TestOnly.GoodsNotReleasedP5View

import java.time.LocalDateTime
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

  private val message: IE051Data = IE051Data(
    IE051MessageData(
      TransitOperationIE051(mrn, LocalDateTime.now(), "G1", "Guarantee not valid")
    )
  )

  private val sections                    = arbitrary[Seq[Section]].sample.value
  private val goodsNotReleasedP5ViewModel = new GoodsNotReleasedP5ViewModel(sections, lrn.toString)

  private val routes = controllers.testOnly.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureIdP5, lrn, messageId).url

  "GoodsNotReleasedP5Controller Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockDepartureP5MessageService.getMessageWithMessageId[IE051Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockGoodsNotReleasedP5ViewModelProvider.apply(any(), any())(any(), any(), any())).thenReturn(goodsNotReleasedP5ViewModel)

      val request = FakeRequest(GET, routes)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[GoodsNotReleasedP5View]

      contentAsString(result) mustEqual
        view(goodsNotReleasedP5ViewModel)(request, messages, frontendAppConfig).toString
    }
  }
}
