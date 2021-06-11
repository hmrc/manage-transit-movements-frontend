/*
 * Copyright 2021 HM Revenue & Customs
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

package services

import base.SpecBase
import connectors.BetaAuthorizationConnector
import featureFlags.WhatDoYouWantToDoRadioToggle
import models.EoriNumber
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.{Application, Configuration}

import scala.concurrent.Future

class WhatDoYouWantToDoRadioToggleSpec extends SpecBase {

  private val mockBetaAuthConnector: BetaAuthorizationConnector = mock[BetaAuthorizationConnector]

  "WhatDoYouWantToDoRadioToggleService" - {

    "must return true if the private beta toggle has been enabled and the user is private beta registered" in {

      val application: Application = applicationBuilder()
        .configure(Configuration("microservice.services.features.isPrivateBetaEnabled" -> true))
        .overrides(bind[BetaAuthorizationConnector].toInstance(mockBetaAuthConnector))
        .build()

      val whatDoYouWantToDoRadioToggleService = application.injector.instanceOf[WhatDoYouWantToDoRadioToggle]

      when(mockBetaAuthConnector.getBetaUser(any())(any())).thenReturn(Future.successful(true))

      whatDoYouWantToDoRadioToggleService.displayGoLiveButtons(EoriNumber("test")).futureValue mustBe true
    }

    "must return false if the private beta has been enabled and the user is not private beta registered" in {

      val application: Application = applicationBuilder()
        .configure(Configuration("microservice.services.features.isPrivateBetaEnabled" -> true))
        .overrides(bind[BetaAuthorizationConnector].toInstance(mockBetaAuthConnector))
        .build()

      val whatDoYouWantToDoRadioToggleService = application.injector.instanceOf[WhatDoYouWantToDoRadioToggle]

      when(mockBetaAuthConnector.getBetaUser(any())(any())).thenReturn(Future.successful(false))

      whatDoYouWantToDoRadioToggleService.displayGoLiveButtons(EoriNumber("test")).futureValue mustBe false
    }

    "must return true if the private beta has been disabled" in {

      val application: Application = applicationBuilder()
        .configure(Configuration("microservice.services.features.isPrivateBetaEnabled" -> false))
        .build()

      val whatDoYouWantToDoRadioToggleService = application.injector.instanceOf[WhatDoYouWantToDoRadioToggle]

      whatDoYouWantToDoRadioToggleService.displayGoLiveButtons(EoriNumber("test")).futureValue mustBe true
    }
  }
}
