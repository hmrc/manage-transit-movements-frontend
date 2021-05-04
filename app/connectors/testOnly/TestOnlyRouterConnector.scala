/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.mvc.Headers
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterConnector @Inject()(val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def submitInboundMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val routerUrl = s"${config.routerUrl}/messages"

    val header = headers.headers.filter(x =>
      x._1.equalsIgnoreCase("X-Message-Recipient") || x._1.equalsIgnoreCase("X-Message-Type") || x._1.equalsIgnoreCase("Content-Type"))

    http.POSTString[HttpResponse](routerUrl, requestData.toString, header)
  }

  private def addHeaders()(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = Seq("Content-Type" -> "application/xml", "Channel" -> "web")

  def createArrivalNotificationMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }

  def resubmitArrivalNotificationMessage(requestData: NodeSeq, arrivalId: String, headers: Headers)(
    implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/$arrivalId"

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.PUTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }

  def submitMessageToCore(requestData: NodeSeq, arrivalId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/$arrivalId/messages"

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }
}
