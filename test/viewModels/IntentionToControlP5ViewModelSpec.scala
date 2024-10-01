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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{CC060CType, RequestedDocumentType, TypeOfControlsType}
import generators.Generators
import models.referenceData.ControlType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.IntentionToControlP5ViewModel.IntentionToControlP5ViewModelProvider

import scala.concurrent.Future

class IntentionToControlP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  "IntentionToControlP5ViewModel" - {

    val typeOfControls    = Seq(TypeOfControlsType(1, "44", None))
    val controlType44     = ControlType("44", "")
    val requestedDocument = Seq(RequestedDocumentType(1, "44", None))

    "when no requested documents" - {

      val x = arbitrary[CC060CType].sample.value

      val message = x
        .copy(TypeOfControls = typeOfControls)
        .copy(RequestedDocument = Nil)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new IntentionToControlP5ViewModelProvider
      val result            = viewModelProvider.apply(message)

      "must return correct section length" in {
        result.sections.length `mustBe` 1
      }

      "must return correct title and heading" in {
        result.titleAndHeading `mustBe` messages("departure.ie060.message.prelodged.titleAndHeading")
      }
      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` messages("departure.ie060.message.prelodged.paragraph1")
        result.paragraph2 `mustBe` messages("departure.ie060.message.prelodged.paragraph2")
        result.paragraph3 `mustBe` messages("departure.ie060.message.prelodged.paragraph3")
      }
      "must return correct end paragraph" in {
        result.type0LinkPrefix `mustBe` messages("departure.ie060.message.paragraph4.prefix")
        result.type0LinkText `mustBe` messages("departure.ie060.message.paragraph4.linkText")
        result.type0LinkTextSuffix `mustBe` messages("departure.ie060.message.paragraph4.suffix")
      }
    }

    "when there is information requested" - {
      val x = arbitrary[CC060CType].sample.value

      val message = x.copy(
        TypeOfControls = typeOfControls,
        RequestedDocument = requestedDocument
      )

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new IntentionToControlP5ViewModelProvider
      val result            = viewModelProvider.apply(message)

      "must not render type of control if present" in {
        result.sections.length `mustBe` 2

        result.sections(1).rows.size `mustBe` 2
        result.sections(1).sectionTitle.value `mustBe` "Control information 1"
      }

      "must return correct title and heading" in {
        result.titleAndHeading `mustBe` messages("departure.ie060.message.prelodged.requestedDocuments.titleAndHeading")
      }

      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph1")
        result.paragraph2 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph2")
        result.paragraph3 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph3")
      }
    }
  }

}
