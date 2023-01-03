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

package controllers.departure

import akka.util.ByteString
import base.SpecBase
import connectors.DeparturesMovementConnector
import controllers.departure.{routes => departureRoutes}
import generators.Generators
import models.DepartureId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AccompanyingDocumentPDFControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val wsResponse: AhcWSResponse                            = mock[AhcWSResponse]
  val mockDeparturesMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(wsResponse)
    reset(mockDeparturesMovementConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDeparturesMovementConnector)
      )

  "AccompanyingDocumentPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {

        val pdfAsBytes: Array[Byte] = Seq.fill(10)(Byte.MaxValue).toArray

        val expectedHeaders = Map(CONTENT_TYPE -> Seq("application/pdf"), CONTENT_DISPOSITION -> Seq("TAD_123"), "OtherHeader" -> Seq("value"))

        when(wsResponse.status) thenReturn 200
        when(wsResponse.bodyAsBytes) thenReturn ByteString(pdfAsBytes)
        when(wsResponse.headers) thenReturn expectedHeaders

        when(mockDeparturesMovementConnector.getPDF(any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val departureId = DepartureId(0)

        val request = FakeRequest(GET, departureRoutes.AccompanyingDocumentPDFController.getPDF(departureId).url)
          .withSession("authToken" -> "BearerToken")

        val result = route(app, request).value

        status(result) mustEqual OK
        headers(result).get(CONTENT_TYPE).value mustEqual "application/pdf"
        headers(result).get(CONTENT_DISPOSITION).value mustBe "TAD_123"
      }

      "must redirect to TechnicalDifficultiesController if connector returns error" in {
        val errorCode = Gen.oneOf(300, 500).sample.value

        when(wsResponse.status) thenReturn errorCode

        when(mockDeparturesMovementConnector.getPDF(any())(any()))
          .thenReturn(Future.successful(wsResponse))

        val departureId = DepartureId(0)

        val request = FakeRequest(GET, departureRoutes.AccompanyingDocumentPDFController.getPDF(departureId).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }
    }
  }
}
