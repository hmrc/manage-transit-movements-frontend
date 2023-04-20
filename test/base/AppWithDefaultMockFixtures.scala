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

package base

import controllers.actions._
import models.departureP5.IE060Data
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{DepartureP5MessageService, ReferenceDataService}

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar {
  self: TestSuite =>

  final val mockGoodsUnderControlActionProvider = mock[GoodsUnderControlActionProvider]
  final val mockReferenceDataService            = mock[ReferenceDataService]
  final val mockDepartureP5MessageService       = mock[DepartureP5MessageService]

  override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  // Override to provide custom binding
  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction]
      )

  protected def goodsUnderControlAction(departureIdP5: String, message: IE060Data, customsOffice: CustomsOffice): Unit = {
    when(mockDepartureP5MessageService.getGoodsUnderControl(any())(any(), any())).thenReturn(Future.successful(Some(message)))
    when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))
    when(mockGoodsUnderControlActionProvider.apply(any())) thenReturn new FakeGoodsUnderControlAction(departureIdP5,
                                                                                                      mockDepartureP5MessageService,
                                                                                                      mockReferenceDataService
    )
  }
}
