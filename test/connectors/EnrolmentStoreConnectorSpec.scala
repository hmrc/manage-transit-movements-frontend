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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import helper.WireMockServerHandler
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._

import scala.concurrent.Future

class EnrolmentStoreConnectorSpec extends SpecBase with WireMockServerHandler {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.enrolment-store-proxy.port" -> server.port())

  lazy val connector: EnrolmentStoreConnector = app.injector.instanceOf[EnrolmentStoreConnector]

  val credId        = "testCredId"
  val groupId       = "testGroupId"
  val enrolmentCode = "testCode"
  val enrolmentKey  = "HMCE-NCTS-ORG"

  val withNCTSGrpEnrolment: String =
    """
      | {
      |    "startRecord": 1,
      |    "totalRecords": 2,
      |    "enrolments": [
      |        {
      |           "service": "IR-SA",
      |           "state": "Activated",
      |           "friendlyName": "My First Client's SA Enrolment",
      |           "enrolmentDate": "2018-10-05T14:48:00.000Z",
      |           "failedActivationCount": 1,
      |           "activationDate": "2018-10-13T17:36:00.000Z",
      |           "identifiers": [
      |              {
      |                 "key": "UTR",
      |                 "value": "1234567890"
      |              }
      |           ]
      |        },
      |        {
      |           "service": "HMCE-NCTS-ORG",
      |           "state": "Activated",
      |           "friendlyName": "My First Client's NCTS Enrolment",
      |           "enrolmentDate": "2018-10-05T14:48:00.000Z",
      |           "failedActivationCount": 1,
      |           "activationDate": "2018-10-13T17:36:00.000Z",
      |           "identifiers": [
      |              {
      |                 "key": "UTR",
      |                 "value": "1234567890"
      |              }
      |           ]
      |        }
      |    ]
      |}
      |""".stripMargin

  val withOutGrpEnrolment: String =
    """
      | {
      |    "startRecord": 1,
      |    "totalRecords": 2,
      |    "enrolments": [
      |        {
      |           "service": "IR-SA",
      |           "state": "Activated",
      |           "friendlyName": "My First Client's SA Enrolment",
      |           "enrolmentDate": "2018-10-05T14:48:00.000Z",
      |           "failedActivationCount": 1,
      |           "activationDate": "2018-10-13T17:36:00.000Z",
      |           "identifiers": [
      |              {
      |                 "key": "UTR",
      |                 "value": "1234567890"
      |              }
      |           ]
      |        },
      |        {
      |           "service": "IR-CT",
      |           "state": "Activated",
      |           "friendlyName": "My First Client's CT Enrolment",
      |           "enrolmentDate": "2018-10-05T14:48:00.000Z",
      |           "failedActivationCount": 1,
      |           "activationDate": "2018-10-13T17:36:00.000Z",
      |           "identifiers": [
      |              {
      |                 "key": "UTR",
      |                 "value": "1234567890"
      |              }
      |           ]
      |        }
      |    ]
      |}
      |""".stripMargin

  "EnrolmentStoreConnector" - {

    "checkGroupEnrolments is called" - {

      "return true when an NCTS enrolment is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey"))
            .willReturn(okJson(withNCTSGrpEnrolment))
        )

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        await(result) mustBe true
      }

      "return false when no NCTS enrolment is present" in {
        server.stubFor(
          get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey"))
            .willReturn(okJson(withOutGrpEnrolment))
        )

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        await(result) mustBe false
      }

      "return false when the API call returns a no content" in {
        server.stubFor(get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey")) willReturn {
          aResponse().withStatus(NO_CONTENT)
        })

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        await(result) mustBe false
      }

      "return false when the API call returns 404 NOT_FOUND" in {
        server.stubFor(get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey")) willReturn {
          aResponse().withStatus(NOT_FOUND)
        })

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        await(result) mustBe false
      }

      "return false when the API call returns 400 BAD_REQUEST with message INVALID_GROUP_ID" in {
        val body = Json.parse("""
            |{
            |  "code": "INVALID_GROUP_ID",
            |  "message": "Invalid group ID"
            |}
            |""".stripMargin)

        server.stubFor(get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey")) willReturn {
          aResponse().withStatus(BAD_REQUEST).withBody(Json.stringify(body))
        })

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        await(result) mustBe false
      }

      "throw exception when the API call returns another 4xx/5xx" in {
        forAll(Gen.choose(400, 599).retryUntil(_ != 404)) {
          status =>
            server.stubFor(
              get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey")) willReturn {
                aResponse().withStatus(status)
              }
            )

            val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

            an[Exception] mustBe thrownBy(result.futureValue)
        }
      }

      "throw exception when the API call returns 200 and invalid JSON" in {
        server.stubFor(get(urlEqualTo(s"/enrolment-store-proxy/enrolment-store/groups/$groupId/enrolments?type=principal&service=$enrolmentKey")) willReturn {
          val response = aResponse().withStatus(OK)
          response.withBody("""
                              | {
                              |   invalid
                              |}
                              |""".stripMargin)

        })

        val result: Future[Boolean] = connector.checkGroupEnrolments(groupId, "HMCE-NCTS-ORG")

        an[Exception] mustBe thrownBy(result.futureValue)
      }
    }
  }

}
