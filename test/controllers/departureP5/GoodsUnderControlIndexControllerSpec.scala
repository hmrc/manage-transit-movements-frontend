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

package controllers.departureP5

import base.SpecBase
import generators.Generators
import models.departureP5.IE060MessageType.{GoodsUnderControl, GoodsUnderControlRequestedDocuments, IntentionToControl}
import models.departureP5._
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class GoodsUnderControlIndexControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService      = mock[ReferenceDataService]
  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val customsOffice = CustomsOffice("GB00006", "UK", None)

  "GoodsUnderControlIndexController" - {

    s"when notification type 0 and requested documents not present must redirect to correct controller" in {
      val notificationType = GoodsUnderControl
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(Some("CD3232"),
                                Some("AB123"),
                                LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
                                notificationType
          ),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          None
        )
      )
      when(mockDepartureP5MessageService.getMessageWithMessageId[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

      val request = FakeRequest(GET, controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.departureP5.routes.GoodsUnderControlP5Controller.noRequestedDocuments(departureIdP5, messageId).url
    }

    s"when notification type 1 and requested documents present must redirect to correct controller" in {
      val notificationType = GoodsUnderControlRequestedDocuments
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(Some("CD3232"),
                                Some("AB123"),
                                LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
                                notificationType
          ),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
        )
      )
      when(mockDepartureP5MessageService.getMessageWithMessageId[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

      val request = FakeRequest(GET, controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.departureP5.routes.GoodsUnderControlP5Controller.requestedDocuments(departureIdP5, messageId).url
    }

    s"when notification type 1 must redirect to correct controller" in {
      val notificationType = GoodsUnderControlRequestedDocuments
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(Some("CD3232"),
                                Some("AB123"),
                                LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
                                notificationType
          ),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
        )
      )
      when(mockDepartureP5MessageService.getMessageWithMessageId[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      val request = FakeRequest(GET, controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.departureP5.routes.GoodsUnderControlP5Controller.requestedDocuments(departureIdP5, messageId).url
    }

    s"when notification type 2 and no control information requested - must redirect to noInformationRequested controller" in {
      val notificationType = IntentionToControl
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(Some("CD3232"),
                                Some("AB123"),
                                LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
                                notificationType
          ),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          None
        )
      )
      when(mockDepartureP5MessageService.getMessageWithMessageId[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

      val request = FakeRequest(GET, controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.departureP5.routes.IntentionToControlP5Controller
        .noInformationRequested(departureIdP5, messageId)
        .url
    }

    s"when notification type 2 and control information requested - must redirect to informationRequested controller" in {
      val notificationType = IntentionToControl
      val message: IE060Data = IE060Data(
        IE060MessageData(
          TransitOperationIE060(Some("CD3232"),
                                Some("AB123"),
                                LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME),
                                notificationType
          ),
          CustomsOfficeOfDeparture("22323323"),
          Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
          Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
        )
      )
      when(mockDepartureP5MessageService.getMessageWithMessageId[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
      when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
        .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))

      val request = FakeRequest(GET, controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureIdP5, messageId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.departureP5.routes.IntentionToControlP5Controller.informationRequested(departureIdP5, messageId).url
    }
  }

}
