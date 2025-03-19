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

package viewModels.P5.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{CC060CType, RequestedDocumentType, TypeOfControlsType}
import generators.Generators
import models.referenceData.{ControlType, CustomsOffice, RequestedDocumentType as RequestedDocumentTypeRef}
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
  private val customsOffice                          = arbitrary[CustomsOffice].sample.value
  private val customsReferenceId                     = "CD123"

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

    val requestedDocumentType = RequestedDocumentTypeRef("C620", "")

    "when no requested documents" - {

      val x = arbitrary[CC060CType].sample.value

      val message = x
        .copy(TypeOfControls = typeOfControls)
        .copy(RequestedDocument = Nil)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new IntentionToControlP5ViewModelProvider
      val result            = viewModelProvider.apply(message, customsOffice)

      "must return correct section length" in {
        result.sections.length `mustBe` 1
      }

      "must return correct title and heading" in {
        result.title `mustBe` messages("departure.ie060.message.prelodged.title")
        result.heading `mustBe` messages("departure.ie060.message.prelodged.heading")
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
      val result            = viewModelProvider.apply(message, customsOffice)

      "must not render type of control if present" in {
        result.sections.length `mustBe` 2

        result.sections(1).rows.size `mustBe` 2
        result.sections(1).sectionTitle.value `mustBe` "Control information 1"
      }

      "must return correct title and heading" in {
        result.title `mustBe` messages("departure.ie060.message.prelodged.requestedDocuments.title")
        result.heading `mustBe` messages("departure.ie060.message.prelodged.requestedDocuments.heading")
      }

      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph1")
        result.paragraph2 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph2")
        result.paragraph3 `mustBe` messages("departure.ie060.message.requestedDocuments.prelodged.paragraph3")
      }
    }

    "customsOfficeContent" - {
      val x = arbitrary[CC060CType].sample.value

      val message = x.copy(
        TypeOfControls = typeOfControls,
        RequestedDocument = requestedDocument
      )
      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

      val viewModelProvider = new IntentionToControlP5ViewModelProvider

      def viewModel(customsOffice: CustomsOffice): IntentionToControlP5ViewModel =
        viewModelProvider.apply(message, customsOffice)

      "When Customs office name, telephone and email exists" - {
        "must return correct message" in {
          val customsOfficeName = "Dover"
          val telephoneNo       = Some("00443243543")
          val email             = Some("test123@gmail.com")

          val customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, email)

          when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
          when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

          val result = viewModel(customsOffice).customsOfficeContent

          result `mustBe` s"You must share the requested documentation with the customs office of departure. Contact Customs at Dover (CD123) on 00443243543 or test123@gmail.com."
        }
      }

      "When Customs Office when name and email are available and  telephone is unavailable" - {
        "must return correct message" in {
          val customsOfficeName = "Dover"
          val email             = Some("test123@gmail.com")
          val customsOffice     = CustomsOffice(customsReferenceId, customsOfficeName, None, email)

          when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
          when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
          val result = viewModel(customsOffice).customsOfficeContent

          result `mustBe` s"You must share the requested documentation with the customs office of departure. Contact Customs at Dover (CD123) on test123@gmail.com."
        }
      }

      "When Customs Office name and telephone are available but email is unavailable" - {
        "must return correct message" in {
          val customsOfficeName = "Dover"
          val telephoneNo       = Some("00443243543")
          val customsOffice     = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, None)

          when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
          when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

          val result = viewModel(customsOffice).customsOfficeContent

          result `mustBe` s"You must share the requested documentation with the customs office of departure. Contact Customs at Dover (CD123) on 00443243543."
        }
      }

      "When Customs Office name available but telephone and email are unavailable" - {
        "must return correct message" in {
          val customsOfficeName = "Dover"
          val customsOffice     = CustomsOffice(customsReferenceId, customsOfficeName, None, None)

          when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
          when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))

          val result = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, None, None)).customsOfficeContent

          result `mustBe` s"You must share the requested documentation with the customs office of departure. Contact Customs at Dover (CD123)."
        }
      }
    }
  }

}
