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
import generated._
import generators.Generators
import models.referenceData.ControlType
import models.referenceData.{RequestedDocumentType => RequestedDocumentTypeRef}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsUnderControlP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  "GoodsUnderControlP5ViewModel" - {

    val typeOfControls = Seq(
      TypeOfControlsType("1", "44", None),
      TypeOfControlsType("2", "45", Some("Desc1"))
    )

    val controlType44 = ControlType("44", "")

    val requestedDocuments = Seq(
      RequestedDocumentType("1", "C620", None)
    )

    val requestedDocumentType = RequestedDocumentTypeRef("C620", "")

    "when no requested documents and type 0" - {
      val x = arbitrary[CC060CType].sample.value

      val message = x
        .copy(TransitOperation = x.TransitOperation.copy(notificationType = "0"))
        .copy(TypeOfControls = typeOfControls)
        .copy(RequestedDocument = Nil)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message).futureValue

      "must render correct number of sections" in {
        result.sections.length `mustBe` 3

        result.sections(1).sectionTitle.value `mustBe` "Control information 1"
        result.sections(1).rows.size `mustBe` 1

        result.sections(2).sectionTitle.value `mustBe` "Control information 2"
        result.sections(2).rows.size `mustBe` 2
      }

      "must return correct title" in {
        result.title `mustBe` "Goods under control"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Goods under control"
      }
      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` "Customs have placed this declaration under control while they carry out further checks. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 `mustBe` "While under control, the goods will remain under supervision at the office of destination."
        result.paragraph3 `mustBe` "Once Customs have completed their checks, they will notify you with the outcome."
      }
      "must return correct end paragraph" in {
        result.type0LinkPrefix `mustBe` "You must wait for the outcome of Customs’ checks."
        result.type0LinkText `mustBe` "Check your departure declarations"
        result.type0LinkTextSuffix `mustBe` "for further updates."
      }
    }

    "when no requested documents and type 1" - {
      val x = arbitrary[CC060CType].sample.value

      val message = x
        .copy(TransitOperation = x.TransitOperation.copy(notificationType = "1"))
        .copy(TypeOfControls = typeOfControls)
        .copy(RequestedDocument = Nil)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message).futureValue

      "must render correct number of sections" in {
        result.sections.length `mustBe` 1
      }

      "must return correct title" in {
        result.title `mustBe` "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 `mustBe` "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 `mustBe` "You must contact the office of destination directly to share the requested documentation."
      }
    }

    "when there is requested documents and type 0" - {
      val x = arbitrary[CC060CType].sample.value

      val message = x
        .copy(TransitOperation = x.TransitOperation.copy(notificationType = "0"))
        .copy(TypeOfControls = Nil)
        .copy(RequestedDocument = requestedDocuments)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message).futureValue

      "must render correct number of sections" in {
        result.sections.length `mustBe` 2
        result.sections(1).sectionTitle.value `mustBe` "Requested document 1"
      }
      "must return correct title" in {
        result.title `mustBe` "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 `mustBe` "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 `mustBe` "You must contact the office of destination directly to share the requested documentation."
      }
    }

    "when there is requested documents and type 1" - {
      val x = arbitrary[CC060CType].retryUntil(_.RequestedDocument.nonEmpty).sample.value

      val message = x
        .copy(TransitOperation = x.TransitOperation.copy(notificationType = "1"))
        .copy(TypeOfControls = Nil)
        .copy(RequestedDocument = requestedDocuments)

      when(mockReferenceDataService.getControlType(any())(any(), any())).thenReturn(Future.successful(controlType44))
      when(mockReferenceDataService.getRequestedDocumentType(any())(any(), any())).thenReturn(Future.successful(requestedDocumentType))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Left("22323323")))

      val viewModelProvider = new GoodsUnderControlP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message).futureValue

      "must render correct number of sections" in {
        result.sections.length `mustBe` 2
        result.sections(1).sectionTitle.value `mustBe` "Requested document 1"
      }
      "must return correct title" in {
        result.title `mustBe` "Goods under control - document requested"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Goods under control - document requested"
      }
      "must return correct paragraphs" in {
        result.paragraph1 `mustBe` "Customs have placed this declaration under control and requested further documentation. This is because of a possible discrepancy or risk to health and safety."
        result.paragraph2 `mustBe` "While awaiting the documentation, the goods will remain under supervision at the office of destination."
        result.paragraph3 `mustBe` "You must contact the office of destination directly to share the requested documentation."
      }
    }
  }

}
