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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generators.Generators
import models.referenceData.{IdentificationType, Nationality}
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

class IncidentP5TranshipmentHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val refDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(refDataService))

  "IncidentAnswersHelper" - {

    val transhipment = arbitraryIncidentType02.arbitrary.sample.value.Transhipment.get

    "rows" - {
      "registeredCountryRow" - {
        "must return a row with description when ref data look up is successful" in {
          val description = "description"
          val nationality = transhipment.TransportMeans.nationality

          when(refDataService.getNationality(any())(any(), any()))
            .thenReturn(Future.successful(Nationality(nationality, description)))

          val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)
          val result = helper.registeredCountryRow.futureValue.value

          result.key.value mustEqual "Registered country"
          result.value.value mustEqual description
          result.actions must not be defined
        }

        "must throw an exception when ref data look up cannot find description" in {
          val refDataService: ReferenceDataService = mock[ReferenceDataService]
          when(refDataService.getNationality(any())(any(), any())).thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)

          whenReady(helper.registeredCountryRow.failed) {
            result => result mustBe a[NoReferenceDataFoundException]
          }
        }
      }

      "identificationTypeRow" - {
        "must return a row with description when ref data look up is successful" in {
          val description        = "description"
          val identificationType = transhipment.TransportMeans.typeOfIdentification

          when(refDataService.getIdentificationType(any())(any(), any()))
            .thenReturn(Future.successful(IdentificationType(identificationType, description)))

          val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)
          val result = helper.identificationTypeRow.futureValue.value

          result.key.value mustEqual "Identification type"
          result.value.value mustEqual description
          result.actions must not be defined
        }

        "must throw an exception when ref data look up cannot find description" in {
          val refDataService: ReferenceDataService = mock[ReferenceDataService]

          when(refDataService.getIdentificationType(any())(any(), any()))
            .thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)

          whenReady(helper.identificationTypeRow.failed) {
            result => result mustBe a[NoReferenceDataFoundException]
          }
        }
      }

      "identificationRow must return a row" in {
        val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)
        val result = helper.identificationRow.value

        result.key.value mustEqual "Identification"
        result.value.value mustEqual transhipment.TransportMeans.identificationNumber
        result.actions must not be defined
      }

    }

    "sections" - {
      "transhipmentSection" - {
        "must return a static section if Transhipment is defined" in {
          val helper = new IncidentP5TranshipmentHelper(transhipment, refDataService)
          val result = helper.replacementMeansOfTransportSection.futureValue

          result mustBe a[StaticSection]
          result.sectionTitle.get mustEqual "Replacement means of transport"
          result.rows.size mustEqual 3
        }
      }

    }
  }

}
