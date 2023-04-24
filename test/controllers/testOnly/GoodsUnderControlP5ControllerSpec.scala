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
import controllers.actions.{FakeGoodsUnderControlAction, GoodsUnderControlActionProvider}
import generators.Generators
import models.departureP5._
import models.referenceData.CustomsOffice
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
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import viewModels.P5.departure.{CustomsOfficeContactViewModel, GoodsUnderControlP5ViewModel}
import views.html.departure.P5.TestOnlyGoodsUnderControlP5View

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class GoodsUnderControlP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGoodsUnderControlP5ViewModelProvider = mock[GoodsUnderControlP5ViewModelProvider]
  private val mockReferenceDataService                 = mock[ReferenceDataService]
  private val mockDepartureP5MessageService            = mock[DepartureP5MessageService]
  private val mockGoodsUnderControlActionProvider      = mock[GoodsUnderControlActionProvider]

  protected def goodsUnderControlAction(departureIdP5: String,
                                        mockDepartureP5MessageService: DepartureP5MessageService,
                                        mockReferenceDataService: ReferenceDataService
  ): Unit =
    when(mockGoodsUnderControlActionProvider.apply(any())) thenReturn new FakeGoodsUnderControlAction(departureIdP5,
                                                                                                      mockDepartureP5MessageService,
                                                                                                      mockReferenceDataService
    )
  lazy val goodsUnderControlController: String = controllers.testOnly.routes.GoodsUnderControlP5Controller.onPageLoad(departureIdP5).url
  private val sections                         = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
    reset(mockGoodsUnderControlP5ViewModelProvider)
    reset(mockGoodsUnderControlActionProvider)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsUnderControlP5ViewModelProvider].toInstance(mockGoodsUnderControlP5ViewModelProvider))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val customsReferenceNumber = Gen.alphaNumStr.sample.value
  private val customsOffice          = arbitrary[CustomsOffice].sample.value

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "0"),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
        )
      )
      when(mockDepartureP5MessageService.getGoodsUnderControl(any())(any(), any())).thenReturn(Future.successful(Some(message)))
      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))
      when(mockGoodsUnderControlP5ViewModelProvider.apply(any())(any()))
        .thenReturn(GoodsUnderControlP5ViewModel(sections, "0"))

      goodsUnderControlAction(departureIdP5, mockDepartureP5MessageService, mockReferenceDataService)

      val goodsUnderControlP5ViewModel  = new GoodsUnderControlP5ViewModel(sections, "0")
      val customsOfficeContactViewModel = CustomsOfficeContactViewModel(customsReferenceNumber, Some(customsOffice))

      val request = FakeRequest(GET, goodsUnderControlController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[TestOnlyGoodsUnderControlP5View]

      contentAsString(result) mustEqual
        view(goodsUnderControlP5ViewModel, departureIdP5, customsOfficeContactViewModel)(request, messages).toString
    }
  }
}
