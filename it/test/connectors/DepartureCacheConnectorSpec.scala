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

import com.github.tomakehurst.wiremock.client.WireMock.*
import generated.Number12
import itbase.{ItSpecBase, WireMockServerHandler}
import models.FunctionalError.FunctionalErrorWithSection
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.departureP5.BusinessRejectionType.AmendmentRejection
import models.departureP5.Rejection.{IE055Rejection, IE056Rejection}
import models.{FunctionalErrorType, InvalidDataItem}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, JsBoolean, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse

class DepartureCacheConnectorSpec extends ItSpecBase with WireMockServerHandler {

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

        result mustEqual true
      }

      "must return false when response body contains false" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(rejection))))
            .willReturn(okJson(Json.stringify(JsBoolean(false))))
        )

        val result: Boolean = await(connector.isRejectionAmendable(lrn.toString, rejection))

        result mustEqual false
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

        result.status mustEqual OK
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

        result.status mustEqual OK
      }
    }

    "convertErrors" - {
      val url = s"/manage-transit-movements-departure-cache/messages/rejection"

      val input = Seq(
        FunctionalErrorType(
          errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
          errorCode = Number12.toString,
          errorReason = "BR20004",
          originalAttributeValue = Some("GB635733627000")
        ),
        FunctionalErrorType(
          errorPointer = "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
          errorCode = Number12.toString,
          errorReason = "BR20005",
          originalAttributeValue = None
        )
      )

      val output = Json
        .parse("""
          |[
          |  {
          |    "error" : "12",
          |    "businessRuleId" : "BR20004",
          |    "section" : "Trader details",
          |    "invalidDataItem" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber",
          |    "invalidAnswer" : "GB635733627000"
          |  },
          |  {
          |    "error" : "12",
          |    "businessRuleId" : "BR20005",
          |    "invalidDataItem" : "/CC015C/HolderOfTheTransitProcedure/identificationNumber"
          |  }
          |]
          |""".stripMargin)
        .as[JsArray]

      "must return converted errors" in {
        server.stubFor(
          post(urlEqualTo(url))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(input))))
            .willReturn(okJson(Json.stringify(output)))
        )

        val result: FunctionalErrorsWithSection = await(connector.convertErrors(input))

        val expectedResult = FunctionalErrorsWithSection(
          Seq(
            FunctionalErrorWithSection(
              error = "12",
              businessRuleId = "BR20004",
              section = Some("Trader details"),
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = Some("GB635733627000")
            ),
            FunctionalErrorWithSection(
              error = "12",
              businessRuleId = "BR20005",
              section = None,
              invalidDataItem = InvalidDataItem("/CC015C/HolderOfTheTransitProcedure/identificationNumber"),
              invalidAnswer = None
            )
          )
        )

        result mustEqual expectedResult
      }
    }
  }

}
