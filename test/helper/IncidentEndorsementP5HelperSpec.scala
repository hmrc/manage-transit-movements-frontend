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
import generated.EndorsementType02
import generators.Generators
import models.referenceData.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import utils.IncidentEndorsementP5Helper
import viewModels.sections.Section.StaticSection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentEndorsementP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(refDataService))

  "IncidentEndorsementAnswersHelper" - {

    val endorsementType03 = arbitrary[EndorsementType02].sample.value

    "rows" - {
      "endorsementDateRow" - {
        "must return a row" in {
          val endorsement = endorsementType03.copy(date = XMLCalendar("2022-07-15"))

          val helper = new IncidentEndorsementP5Helper(endorsement, refDataService)
          val result = helper.endorsementDateRow.value

          result.key.value `mustBe` "Endorsement date"
          result.value.value `mustBe` "15 July 2022"
          result.actions must not be defined
        }
      }

      "authorityRow" - {
        "must return a row" in {
          val endorsement = endorsementType03.copy(authority = "authority")

          val helper = new IncidentEndorsementP5Helper(endorsement, refDataService)
          val result = helper.authorityRow.value

          result.key.value `mustBe` "Authority"
          result.value.value `mustBe` "authority"
          result.actions must not be defined
        }
      }

      "endorsementCountryRow" - {
        "must return a row" in {
          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Country("GB", "United Kingdom")))

          val endorsement = endorsementType03.copy(country = "GB")

          val helper = new IncidentEndorsementP5Helper(endorsement, refDataService)
          val result = helper.endorsementCountryRow.futureValue.value

          result.key.value `mustBe` "Country"
          result.value.value `mustBe` "United Kingdom"
          result.actions must not be defined
        }
      }

      "locationRow" - {
        "must return a row" in {
          val endorsement = endorsementType03.copy(place = "location")

          val helper = new IncidentEndorsementP5Helper(endorsement, refDataService)
          val result = helper.locationRow.value

          result.key.value `mustBe` "Location"
          result.value.value `mustBe` "location"
          result.actions must not be defined
        }
      }

    }

    "sections" - {
      "endorsementSection" - {
        "must return a static section" in {
          val endorsement = endorsementType03.copy(place = "location")

          val helper = new IncidentEndorsementP5Helper(endorsement, refDataService)
          val result = helper.endorsementSection.futureValue

          result `mustBe` a[StaticSection]
          result.rows.size `mustBe` 4
        }
      }
    }
  }

}
