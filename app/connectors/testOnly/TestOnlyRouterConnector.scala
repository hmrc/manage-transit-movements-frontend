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

package connectors.testOnly

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.Headers
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterConnector @Inject()(val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  val Log: Logger = Logger(getClass)

  def submitInboundMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val routerUrl = s"${config.routerUrl}/messages"
    Log.debug(s"Implicit Headers From Core (Connector): ${headerCarrier.headers.toString()}")
    Log.debug(s"Explicit Headers From Core (Connector): ${headers.headers.toString()}")

    val header = headers.headers.filter(x => x._1 == "X-Message-Sender" || x._1 == "X-Message-Type" || x._1 == "Content-Type")
    Log.debug(s"updated header : $header")

    http.POSTString[HttpResponse](routerUrl, requestData.toString, header)
  }

  private def addHeaders()(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = Seq("Content-Type" -> "application/xml")

  def createArrivalNotificationMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"

    Log.debug(s"Implicit Headers To Core (Connector): ${headerCarrier.headers.toString()}")
    Log.debug(s"Explicit Headers To Core (Connector): ${headers.headers.toString()}")

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }

  def submitMessageToCore(requestData: NodeSeq, arrivalId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/$arrivalId/messages"

    Log.debug(s"submitMessageToCore:Implicit Headers To Core (Connector): ${headerCarrier.headers.toString()}")
    Log.debug(s"submitMessageToCore:Explicit Headers To Core (Connector): ${headers.headers.toString()}")

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }
}
