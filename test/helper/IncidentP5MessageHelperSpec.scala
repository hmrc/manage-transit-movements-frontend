/*
 * Copyright 2024 HM Revenue & Customs
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

package helper

import base.SpecBase
import generators.Generators
import models.{IncidentCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import utils.IncidentP5MessageHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentP5MessageHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  private val incident = IncidentCode("code", "text")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    when(mockReferenceDataService.getIncidentCode(any())(any(), any()))
      .thenReturn(Future.successful(incident))
  }

  "IncidentP5MessageHelper" - {

    "rows" - {
      "incidentCodeRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val incidentCode = IncidentCode(
                "1",
                "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              )

              val prefix =
                Gen.oneOf(Seq("departure.notification.incidents.incident.code.label", "departure.notification.incident.index.code")).sample.value

              when(mockReferenceDataService.getIncidentCode(any())(any(), any()))
                .thenReturn(Future.successful(incidentCode))

              val helper = new IncidentP5MessageHelper(mockReferenceDataService)
              val result = helper.incidentCodeRow(value, prefix).futureValue.value

              result.key.value mustBe "Incident code"
              result.value.value mustBe
                "1 - The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              result.actions must not be defined
          }
        }
      }

      "incidentDescriptionRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val prefix =
                Gen.oneOf(Seq("departure.notification.incidents.incident.description.label", "departure.notification.incident.index.description")).sample.value

              val helper = new IncidentP5MessageHelper(mockReferenceDataService)
              val result = helper.incidentDescriptionRow(value, prefix).value

              result.key.value mustBe "Description"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }
    }
  }
}
