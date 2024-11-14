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
import com.github.tomakehurst.wiremock.client.WireMock.*
import helper.WireMockServerHandler
import models.departureP5.BusinessRejectionType.AmendmentRejection
import models.departureP5.Rejection
import models.departureP5.Rejection.{IE055Rejection, IE056Rejection}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse

class DepartureCacheConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.manage-transit-movements-departure-cache.port" -> server.port())

  private lazy val connector: DepartureCacheConnector = app.injector.instanceOf[DepartureCacheConnector]

  "DepartureCacheConnector" - {

    "isRejectionAmendable" - {

      val url       = s"/manage-transit-movements-departure-cache/user-answers/$lrn/amendable"
      val rejection = IE055Rejection(departureIdP5)

      "must return true when response body contains true" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(rejection))))
            .willReturn(okJson(Json.stringify(JsBoolean(true))))
        )

        val result: Boolean = await(connector.isRejectionAmendable(lrn.toString, rejection))

        result `mustBe` true
      }

      "must return false when response body contains false" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(rejection))))
            .willReturn(okJson(Json.stringify(JsBoolean(false))))
        )

        val result: Boolean = await(connector.isRejectionAmendable(lrn.toString, rejection))

        result `mustBe` false
      }
    }

    "handleErrors" - {

      val url = s"/manage-transit-movements-departure-cache/user-answers/$lrn/errors"

      val xPaths = Seq("/TransitOperation", "/Authorisations")

      "must return OK when request succeeds" in {
        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(ok())
        )

        val rejection = IE056Rejection(departureIdP5, AmendmentRejection, xPaths)

        val result: HttpResponse = await(connector.handleErrors(lrn.toString, rejection))

        result.status `mustBe` OK
      }
    }

    "prepareForAmendment" - {

      val url = s"/manage-transit-movements-departure-cache/user-answers/$lrn"

      "must return OK when request succeeds" in {
        server.stubFor(
          patch(urlEqualTo(url))
            .willReturn(ok())
        )

        val result: HttpResponse = await(connector.prepareForAmendment(lrn.toString, departureIdP5))

        result.status `mustBe` OK
      }
    }
  }

}
