/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import models.ArrivalId
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.ahc.cache.{CacheableHttpResponseBodyPart, CacheableHttpResponseStatus}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.shaded.ahc.org.asynchttpclient.Response
import play.shaded.ahc.org.asynchttpclient.uri.Uri

import scala.concurrent.Future

class UnloadingPermissionPDFControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val mockArrivalMovementConnector = mock[ArrivalMovementConnector]

  "UnloadingPermissionPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {

        forAll(arbitrary[Array[Byte]]) {
          pdf =>
            val wsResponse: AhcWSResponse = new AhcWSResponse(
              new Response.ResponseBuilder()
                .accumulate(new CacheableHttpResponseStatus(Uri.create("http://uri"), 200, "status text", "protocols!"))
                .accumulate(new CacheableHttpResponseBodyPart(pdf, true))
                .build())

            when(mockArrivalMovementConnector.getPDF(any(), any())(any()))
              .thenReturn(Future.successful(wsResponse))

            val arrivalId = ArrivalId(0)

            val application: Application =
              applicationBuilder()
                .overrides(
                  bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
                )
                .build()

            val request = FakeRequest(
              GET,
              routes.UnloadingPermissionPDFController.getPDF(arrivalId).url
            ).withHeaders(("Authorization", "BearerToken"))

            running(application) {

              val result = route(application, request).value

              status(result) mustEqual OK
            }
        }
      }

      "must return Unauthorized if bearer token is missing" in {

        val arrivalId = ArrivalId(0)

        val application: Application =
          applicationBuilder()
            .overrides(
              bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
            )
            .build()

        val request = FakeRequest(
          GET,
          routes.UnloadingPermissionPDFController.getPDF(arrivalId).url
        )

        running(application) {

          val result = route(application, request).value

          status(result) mustEqual UNAUTHORIZED
        }
      }
    }
  }

}
