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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, urlEqualTo}
import helper.WireMockServerHandler
import models.Movement
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import viewModels.ViewArrivalMovement

import scala.concurrent.ExecutionContext.Implicits.global



class ArrivalNotificationConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private lazy val connector: ArrivalNotificationConnector = app.injector.instanceOf[ArrivalNotificationConnector]
  private val startUrl = "transit-movements-trader-at-destination"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.destination.port" -> server.port()
    )
    .build()

  "ArrivalNotificationConnector" - {
    "must return a successful future response with a view arrival movement" in {

      val expectedResult = ViewArrivalMovement(Map("01-01-2020" ->
        Seq(Movement("test updated","test mrn", "test name", "test office", "test procedure", "test status", Seq("test actions")))))
      val movementResponseJson = buildJson(expectedResult)

      server.stubFor(
        get(urlEqualTo(s"/$startUrl/messages"))
          .willReturn(okJson(movementResponseJson.toString()))
      )


      connector.getArrivalMovements.futureValue mustBe expectedResult

    }
  }

  private def buildJson(viewArrivalMovement: ViewArrivalMovement): JsObject = {

    viewArrivalMovement.tables.map {
      case (date, rows) =>
        Json.obj(
          date -> buildRows(rows)
        )
    }.fold(JsObject.empty)(_ ++ _)
  }

  private def buildRows(rows: Seq[Movement]): Seq[JsObject] = {
    rows.map {
      row =>
        Json.obj(
          "updated" -> row.updated,
          "mrn" -> row.mrn,
          "traderName" -> row.traderName,
          "office" -> row.office,
          "procedure" -> row.procedure,
          "status" -> row.status,
          "actions" -> row.actions
        )
    }
  }
}