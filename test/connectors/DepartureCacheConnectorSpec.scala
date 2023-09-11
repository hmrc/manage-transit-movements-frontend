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

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsBoolean, JsString, Json}
import play.api.test.Helpers._

class DepartureCacheConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.manage-transit-movements-departure-cache.port" -> server.port())

  private lazy val connector: DepartureCacheConnector = app.injector.instanceOf[DepartureCacheConnector]

  "DepartureCacheConnector" - {

    "isDeclarationAmendable" - {

      val url    = s"/manage-transit-movements-departure-cache/x-paths/$lrn/is-declaration-amendable"
      val xPaths = Seq("foo", "bar")

      "must return true when response body contains true" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(JsArray(xPaths.map(JsString)))))
            .willReturn(okJson(Json.stringify(JsBoolean(true))))
        )

        val result: Boolean = await(connector.isDeclarationAmendable(lrn.toString, xPaths))

        result mustBe true
      }

      "must return false when response body contains false" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(JsArray(xPaths.map(JsString)))))
            .willReturn(okJson(Json.stringify(JsBoolean(false))))
        )

        val result: Boolean = await(connector.isDeclarationAmendable(lrn.toString, xPaths))

        result mustBe false
      }
    }

    "doesDeclarationExist" - {

      val url = s"/manage-transit-movements-departure-cache/does-cache-exists-for-lrn/$lrn"

      "must return true when response body contains true" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.stringify(JsBoolean(true))))
        )

        val result: Boolean = await(connector.doesDeclarationExist(lrn.toString))

        result mustBe true
      }

      "must return false when response body contains false" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.stringify(JsBoolean(false))))
        )

        val result: Boolean = await(connector.doesDeclarationExist(lrn.toString))

        result mustBe false
      }
    }

    "handleGuaranteeRejection" - {

      val url = s"/manage-transit-movements-departure-cache/x-paths/$lrn/handle-guarantee-errors"

      "must return true when response body contains true" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.stringify(JsBoolean(true))))
        )

        val result: Boolean = await(connector.handleGuaranteeRejection(lrn.toString))

        result mustBe true
      }

      "must return false when response body contains false" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.stringify(JsBoolean(false))))
        )

        val result: Boolean = await(connector.handleGuaranteeRejection(lrn.toString))

        result mustBe false
      }
    }

  }
}
