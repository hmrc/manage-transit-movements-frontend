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
import models.{IdentificationType, Nationality}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import utils.IncidentP5TranshipmentHelper
import viewModels.sections.Section.StaticSection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentP5TranshipmentHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService: ReferenceDataService = mock[ReferenceDataService]
  private val displayIndex                         = 1

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(refDataService))

  "IncidentAnswersHelper" - {

    val transhipment = arbitraryIncidentType03.arbitrary.sample.value.Transhipment.get

    "rows" - {
      "registeredCountryRow" - {
        "must return a row with description when ref data look up is successful" in {
          when(refDataService.getNationality(any())(any(), any()))
            .thenReturn(Future.successful(Right(Nationality(transhipment.TransportMeans.nationality, "description"))))

          val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
          val result = helper.registeredCountryRow.futureValue.value

          result.key.value mustBe "Registered country"
          result.value.value mustBe "description"
          result.actions must not be defined
        }

        "must return a row with description when ref data look up cannot find description" in {
          when(refDataService.getNationality(any())(any(), any()))
            .thenReturn(Future.successful(Left(transhipment.TransportMeans.nationality)))

          val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
          val result = helper.registeredCountryRow.futureValue.value

          result.key.value mustBe "Registered country"
          result.value.value mustBe transhipment.TransportMeans.nationality
          result.actions must not be defined
        }
      }

      "identificationTypeRow" - {
        "must return a row with description when ref data look up is successful" in {
          when(refDataService.getIdentificationType(any())(any(), any()))
            .thenReturn(Future.successful(Right(IdentificationType(transhipment.TransportMeans.typeOfIdentification, "description"))))

          val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
          val result = helper.identificationTypeRow.futureValue.value

          result.key.value mustBe "Identification type"
          result.value.value mustBe "description"
          result.actions must not be defined
        }

        "must return a row with description when ref data look up cannot find description" in {
          when(refDataService.getIdentificationType(any())(any(), any()))
            .thenReturn(Future.successful(Left(transhipment.TransportMeans.typeOfIdentification)))

          val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
          val result = helper.identificationTypeRow.futureValue.value

          result.key.value mustBe "Identification type"
          result.value.value mustBe transhipment.TransportMeans.typeOfIdentification
          result.actions must not be defined
        }
      }

      "identificationRow must return a row" in {
        val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
        val result = helper.identificationRow.value

        result.key.value mustBe "Identification"
        result.value.value mustBe transhipment.TransportMeans.identificationNumber
        result.actions must not be defined
      }

    }

    "sections" - {
      "transhipmentSection" - {
        "must return a static section if Transhipment is defined" in {
          val helper = new IncidentP5TranshipmentHelper(transhipment, displayIndex, refDataService)
          val result = helper.replacementMeansOfTransportSection.futureValue

          result mustBe a[StaticSection]
          result.sectionTitle.get mustBe "Replacement means of transport"
          result.rows.size mustBe 3
        }
      }

    }
  }
}
