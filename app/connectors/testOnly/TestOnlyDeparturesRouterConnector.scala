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
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.Headers
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyDeparturesRouterConnector @Inject() (val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) extends Logging {

  private def newHeaders(headers: Headers, contentType: String)(implicit hc: HeaderCarrier): HeaderCarrier =
    hc
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(Seq("Content-Type" -> s"application/$contentType", "Channel" -> "web"): _*)

  def createDeclarationMessage(
    requestData: NodeSeq,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.departureUrl}/movements/departures"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers, "xml"),
      ec = ec
    )
  }

  def createDeclarationCancellationMessage(
    requestData: NodeSeq,
    departureId: String,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.departureUrl}/movements/departures/$departureId/messages"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers, "xml"),
      ec = ec
    )
  }

  def submitMessageToCore(
    requestData: JsValue,
    departureId: String,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.testSupportUrl}/movements/departures/$departureId/messages"

    http.POST[JsValue, HttpResponse](serviceUrl, requestData)(
      wts = implicitly,
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers, "json"),
      ec = ec
    )
  }
}
