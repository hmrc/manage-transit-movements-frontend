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
import generated.{CC060CType, RequestedDocumentType}
import generators.Generators
import models.departureP5._
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import viewModels.P5.departure.{CustomsOfficeContactViewModel, GoodsUnderControlP5ViewModel}
import views.html.departureP5.GoodsUnderControlP5View

import scala.concurrent.Future

class GoodsUnderControlP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGoodsUnderControlP5ViewModelProvider = mock[GoodsUnderControlP5ViewModelProvider]
  private val mockReferenceDataService                 = mock[ReferenceDataService]
  private val mockDepartureP5MessageService            = mock[DepartureP5MessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
    reset(mockGoodsUnderControlP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsUnderControlP5ViewModelProvider].toInstance(mockGoodsUnderControlP5ViewModelProvider))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val customsOffice = arbitrary[CustomsOffice].sample.value

  "GoodsUnderControlP5 Controller" - {

    "must return OK and the correct view for a GET when requestedDocuments" in {
      forAll(listWithMaxLength[RequestedDocumentType]()) {
        requestedDocuments =>
          forAll(arbitrary[CC060CType].map(_.copy(RequestedDocument = requestedDocuments))) {
            message =>
              val goodsUnderControlRequestedDocumentsController: String =
                controllers.departureP5.routes.GoodsUnderControlP5Controller.requestedDocuments(departureIdP5, messageId).url

              when(mockDepartureP5MessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
              when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Right(customsOffice)))
              when(mockGoodsUnderControlP5ViewModelProvider.apply(any())(any(), any(), any()))
                .thenReturn(Future.successful(GoodsUnderControlP5ViewModel(sections, requestedDocuments = true, Some(lrn.toString))))

              val goodsUnderControlP5ViewModel  = new GoodsUnderControlP5ViewModel(sections, true, Some(lrn.toString))
              val customsOfficeContactViewModel = CustomsOfficeContactViewModel(Right(customsOffice))

              val request = FakeRequest(GET, goodsUnderControlRequestedDocumentsController)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[GoodsUnderControlP5View]

              contentAsString(result) mustEqual
                view(goodsUnderControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(request, messages).toString
          }
      }
    }

    "must return OK and the correct view for a GET when noRequestedDocuments" in {
      forAll(arbitrary[CC060CType].map(_.copy(RequestedDocument = Nil))) {
        message =>
          val goodsUnderControlNoRequestedDocumentsController: String =
            controllers.departureP5.routes.GoodsUnderControlP5Controller.noRequestedDocuments(departureIdP5, messageId).url

          when(mockDepartureP5MessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Right(customsOffice)))
          when(mockGoodsUnderControlP5ViewModelProvider.apply(any())(any(), any(), any()))
            .thenReturn(Future.successful(GoodsUnderControlP5ViewModel(sections, requestedDocuments = false, Some(lrn.toString))))

          val goodsUnderControlP5ViewModel  = new GoodsUnderControlP5ViewModel(sections, false, Some(lrn.toString))
          val customsOfficeContactViewModel = CustomsOfficeContactViewModel(Right(customsOffice))

          val request = FakeRequest(GET, goodsUnderControlNoRequestedDocumentsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[GoodsUnderControlP5View]

          contentAsString(result) mustEqual
            view(goodsUnderControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(request, messages).toString
      }
    }
  }
}
