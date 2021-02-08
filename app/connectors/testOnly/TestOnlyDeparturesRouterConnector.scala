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
import logging.Logging
import play.api.mvc.Headers
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyDeparturesRouterConnector @Inject()(val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {


  private def addHeaders()(implicit headerCarrier: HeaderCarrier): Seq[(String, String)] = Seq("Content-Type" -> "application/xml", "Channel" -> "web")

  def createDeclarationMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.departureUrl}/movements/departures"

    val newHeaders = headerCarrier
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(addHeaders(): _*)

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads.readRaw, hc = newHeaders, ec = ec)
  }

  //TODO: Not yet implemented (needs updating when we need to get inbound messages - see TestOnlyRouterConnector)
  def submitInboundMessage(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = ???

  //TODO: Not yet implemented (needs updating when we need to send other messages - see TestOnlyRouterConnector)
  def submitMessageToCore(requestData: NodeSeq, departureId: String, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = ???
}
