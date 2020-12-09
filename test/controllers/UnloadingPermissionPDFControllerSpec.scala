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

import akka.util.ByteString
import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import models.ArrivalId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UnloadingPermissionPDFControllerSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val wsResponse: AhcWSResponse                      = mock[AhcWSResponse]
  val mockArrivalMovementConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(wsResponse)
    reset(mockArrivalMovementConnector)
  }

  private val appBuilder =
    applicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
      )

  "UnloadingPermissionPDFController" - {

    "getPDF" - {

      "must return OK and PDF" in {

        forAll(arbitrary[Array[Byte]] suchThat (_.nonEmpty)) {
          pdf =>
            when(wsResponse.status) thenReturn 200
            when(wsResponse.bodyAsBytes) thenReturn ByteString(pdf)

            when(mockArrivalMovementConnector.getPDF(any(), any())(any()))
              .thenReturn(Future.successful(wsResponse))

            val arrivalId = ArrivalId(0)

            val application = appBuilder.build()

            val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
              .withSession(("authToken" -> "BearerToken"))

            running(application) {

              val result = route(application, request).value

              status(result) mustEqual OK
            }
        }
      }

      "must redirect to UnauthorisedController if bearer token is missing" in {

        val arrivalId = ArrivalId(0)

        val application = appBuilder.build()

        val request = FakeRequest(
          GET,
          routes.UnloadingPermissionPDFController.getPDF(arrivalId).url
        )

        running(application) {

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.UnauthorisedController.onPageLoad().url
        }
      }

      "must redirect to TechnicalDifficultiesController if connector returns error" in {

        val genErrorResponse = Gen.oneOf(300, 500)

        forAll(genErrorResponse) {
          errorCode =>
            val wsResponse: AhcWSResponse = mock[AhcWSResponse]
            when(wsResponse.status) thenReturn errorCode

            when(mockArrivalMovementConnector.getPDF(any(), any())(any()))
              .thenReturn(Future.successful(wsResponse))

            val arrivalId = ArrivalId(0)

            val application = appBuilder.build()

            val request = FakeRequest(GET, routes.UnloadingPermissionPDFController.getPDF(arrivalId).url)
              .withSession(("authToken" -> "BearerToken"))

            running(application) {

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual controllers.routes.TechnicalDifficultiesController.onPageLoad().url
            }
        }
      }
    }
  }
}
