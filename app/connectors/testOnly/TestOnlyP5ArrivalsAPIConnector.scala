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

package connectors.testOnly

import config.FrontendAppConfig
import connectors.MovementP5Connector
import play.api.libs.json.JsValue
import play.api.mvc.Headers
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyP5ArrivalsAPIConnector @Inject() (val http: HttpClientV2, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends MovementP5Connector {

  def arrivalOutbound(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = url"${config.commonTransitConventionTradersUrl}movements/arrivals"

    val headerSeq = headers.get("Authorization").map(authorizationHeader).foldLeft(Seq(xmlContentTypeHeader, jsonAcceptHeader))(_ :+ _)
    http
      .post(serviceUrl)
      .setHeader(headerSeq: _*)
      .withBody(requestData)
      .execute[HttpResponse]
  }

  def unloadingOutbound(requestData: NodeSeq, arrivalId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = url"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages"

    val headerSeq =
      headers
        .get("Authorization")
        .map(authorizationHeader)
        .foldLeft(Seq(xmlContentTypeHeader, jsonAcceptHeader, messageTypeHeader(headers.get("X-Message-Type"))))(_ :+ _)
    http
      .post(serviceUrl)
      .setHeader(headerSeq: _*)
      .withBody(requestData)
      .execute[HttpResponse]
  }

  def arrivalInbound(requestData: NodeSeq, arrivalId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = url"${config.transitMovementsUrl}transit-movements/traders/movements/$arrivalId/messages"
    val headerSeq =
      headers
        .get("Authorization")
        .map(authorizationHeader)
        .foldLeft(Seq(xmlContentTypeHeader, jsonAcceptHeader, messageTypeHeader(headers.get("X-Message-Type"))))(_ :+ _)
    http
      .post(serviceUrl)
      .setHeader(headerSeq: _*)
      .withBody(requestData)
      .execute[HttpResponse]
  }

  def getMessage(arrivalId: String, messageId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[JsValue] = {

    val serviceUrl = url"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages/$messageId"

    val headerSeq =
      headers
        .get("Authorization")
        .map(authorizationHeader)
        .foldLeft(Seq(jsonAcceptHeader))(_ :+ _)
    http
      .get(serviceUrl)
      .setHeader(headerSeq: _*)
      .execute[JsValue]
  }
}
