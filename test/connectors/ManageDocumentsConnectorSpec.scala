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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import generators.Generators
import helper.WireMockServerHandler
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ManageDocumentsConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with Generators {

  private lazy val connector: ManageDocumentsConnector = app.injector.instanceOf[ManageDocumentsConnector]
  private val startUrl                                 = "transit-movements-trader-manage-documents"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.transit-movements-trader-manage-documents.port" -> server.port())

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "ManageDocumentsConnectorSpec" - {

    "getTAD" - {

      val departureId = "ABC123"
      val messageId   = "DFG456"

      "must return status Ok" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/$departureId/transit-accompanying-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val result: Future[HttpResponse] = connector.getTAD(departureId, messageId)

        result.futureValue.status mustBe 200
      }

      "must return other error status codes without exceptions" in {

        val genErrorResponse = Gen.oneOf(300, 500).sample.value

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/$departureId/transit-accompanying-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(genErrorResponse)
            )
        )

        val result: Future[HttpResponse] = connector.getTAD(departureId, messageId)

        result.futureValue.status mustBe genErrorResponse
      }
    }

    "getUnloadingPermission" - {

      val arrivalId = "ABC123"
      val messageId = "DFG456"

      "must return status Ok" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/$arrivalId/unloading-permission-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val result: Future[HttpResponse] = connector.getUnloadingPermission(arrivalId, messageId)

        result.futureValue.status mustBe 200
      }

      "must return other error status codes without exceptions" in {

        val genErrorResponse = Gen.oneOf(300, 500).sample.value

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/$arrivalId/unloading-permission-document/$messageId"))
            .willReturn(
              aResponse()
                .withStatus(genErrorResponse)
            )
        )

        val result: Future[HttpResponse] = connector.getUnloadingPermission(arrivalId, messageId)

        result.futureValue.status mustBe genErrorResponse
      }
    }
  }
}
