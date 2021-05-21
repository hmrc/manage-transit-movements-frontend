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
import generators.Generators
import models.EoriNumber
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.{Application, Configuration}

import scala.concurrent.Future

class DisplayDeparturesServiceSpec extends SpecBase with BeforeAndAfterEach with Matchers with Generators {

  private val mockBetaAuthConnector: BetaAuthorizationConnector = mock[BetaAuthorizationConnector]

  "DisplayDeparturesService" - {

    "showDepartures" - {

      "must return true" - {

        "if departures toggle is true" in {

          val application: Application = applicationBuilder()
            .configure(Configuration("microservice.services.features.departureJourney" -> true))
            .overrides(bind[BetaAuthorizationConnector].toInstance(mockBetaAuthConnector))
            .build()

          val displayDeparturesService: DisplayDeparturesService = application.injector.instanceOf[DisplayDeparturesService]

          displayDeparturesService.showDepartures(EoriNumber("test")).futureValue mustBe true
        }

        "if departures toggle is false and user is beta registered" in {

          val application: Application = applicationBuilder()
            .configure(Configuration("microservice.services.features.departureJourney" -> false))
            .overrides(bind[BetaAuthorizationConnector].toInstance(mockBetaAuthConnector))
            .build()

          val displayDeparturesService: DisplayDeparturesService = application.injector.instanceOf[DisplayDeparturesService]

          when(mockBetaAuthConnector.getBetaUser(any())(any())).thenReturn(Future.successful(true))

          displayDeparturesService.showDepartures(EoriNumber("test")).futureValue mustBe true
        }

      }

      "must return false" - {

        "if departures toggle is false and user is not beta registered" in {

          val application: Application = applicationBuilder()
            .configure(Configuration("microservice.services.features.departureJourney" -> false))
            .overrides(bind[BetaAuthorizationConnector].toInstance(mockBetaAuthConnector))
            .build()

          val displayDeparturesService: DisplayDeparturesService = application.injector.instanceOf[DisplayDeparturesService]

          when(mockBetaAuthConnector.getBetaUser(any())(any())).thenReturn(Future.successful(false))

          displayDeparturesService.showDepartures(EoriNumber("test")).futureValue mustBe false

        }
      }
    }
  }

}
