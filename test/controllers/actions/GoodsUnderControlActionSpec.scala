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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import models.departureP5._
import models.referenceData.CustomsOffice
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GoodsUnderControlActionSpec extends SpecBase with BeforeAndAfterEach with AppWithDefaultMockFixtures {

  val mockMessageService: DepartureP5MessageService  = mock[DepartureP5MessageService]
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  val message: IE060Data = IE060Data(
    IE060MessageData(
      TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
      CustomsOfficeOfDeparture("22323323"),
      Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
      Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
    )
  )

  val customsOffice: CustomsOffice = CustomsOffice("GB000060", "name", Some("999"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockMessageService)
  }

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  "GoodsUnderControlAction" - {
    "must return 200 when an unloading permission is available" in {

      when(mockMessageService.getMessage[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      val goodsUnderControlProvider = (new GoodsUnderControlActionProvider(mockMessageService, mockReferenceDataService)(implicitly))(departureIdP5)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = goodsUnderControlProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual OK
    }

    "must return 303 and redirect to technical difficulties when no unloading permission is available" in {

      when(mockMessageService.getMessage[IE060Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(None))
      when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(Some(customsOffice)))

      val goodsUnderControlProvider = (new GoodsUnderControlActionProvider(mockMessageService, mockReferenceDataService)(implicitly))(departureIdP5)

      val testRequest = IdentifierRequest(FakeRequest(GET, "/"), "eori")

      val result: Future[Result] = goodsUnderControlProvider.invokeBlock(testRequest, fakeOkResult)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe routes.ErrorController.technicalDifficulties().url
    }
  }
}
