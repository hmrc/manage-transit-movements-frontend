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
import generated.{CC060CType, RequestedDocumentType}
import generators.Generators
import models.departure.*
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DepartureMessageService, ReferenceDataService}
import viewModels.departure.GoodsUnderControlViewModel.GoodsUnderControlViewModelProvider
import viewModels.departure.GoodsUnderControlViewModel
import views.html.departure.GoodsUnderControlView

import scala.concurrent.Future

class GoodsUnderControlControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGoodsUnderControlViewModelProvider = mock[GoodsUnderControlViewModelProvider]
  private val mockReferenceDataService               = mock[ReferenceDataService]
  private val mockDepartureMessageService            = mock[DepartureMessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureMessageService)
    reset(mockGoodsUnderControlViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsUnderControlViewModelProvider].toInstance(mockGoodsUnderControlViewModelProvider))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  private val customsOffice = arbitrary[CustomsOffice].sample.value

  "GoodsUnderControlP5 Controller" - {

    "must return OK and the correct view for a GET when requestedDocuments" in {
      forAll(listWithMaxLength[RequestedDocumentType]()) {
        requestedDocuments =>
          forAll(arbitrary[CC060CType].map(_.copy(RequestedDocument = requestedDocuments))) {
            message =>
              val goodsUnderControlRequestedDocumentsController: String =
                controllers.departure.routes.GoodsUnderControlController.requestedDocuments(departureIdP5, messageId).url

              when(mockDepartureMessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
              when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
              when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
              when(mockGoodsUnderControlViewModelProvider.apply(any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(GoodsUnderControlViewModel(sections, requestedDocuments = true, Some(lrn.toString), customsOffice)))

              val goodsUnderControlViewModel = new GoodsUnderControlViewModel(sections, true, Some(lrn.toString), customsOffice)

              val request = FakeRequest(GET, goodsUnderControlRequestedDocumentsController)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[GoodsUnderControlView]

              contentAsString(result) mustEqual
                view(goodsUnderControlViewModel, departureIdP5)(request, messages).toString
          }
      }
    }

    "must return OK and the correct view for a GET when noRequestedDocuments" in {
      forAll(arbitrary[CC060CType].map(_.copy(RequestedDocument = Nil))) {
        message =>
          val goodsUnderControlNoRequestedDocumentsController: String =
            controllers.departure.routes.GoodsUnderControlController.noRequestedDocuments(departureIdP5, messageId).url

          when(mockDepartureMessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
          when(mockGoodsUnderControlViewModelProvider.apply(any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(GoodsUnderControlViewModel(sections, requestedDocuments = false, Some(lrn.toString), customsOffice)))

          val goodsUnderControlViewModel = new GoodsUnderControlViewModel(sections, false, Some(lrn.toString), customsOffice)

          val request = FakeRequest(GET, goodsUnderControlNoRequestedDocumentsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[GoodsUnderControlView]

          contentAsString(result) mustEqual
            view(goodsUnderControlViewModel, departureIdP5)(request, messages).toString
      }
    }
  }

}
