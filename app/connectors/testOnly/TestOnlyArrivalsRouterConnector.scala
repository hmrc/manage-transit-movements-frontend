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
import play.api.mvc.Headers
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyArrivalsRouterConnector @Inject() (val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  private def newHeaders(headers: Headers)(implicit hc: HeaderCarrier): HeaderCarrier =
    hc
      .copy(authorization = Some(Authorization(headers.get("Authorization").getOrElse(""))))
      .withExtraHeaders(Seq("Content-Type" -> "application/xml", "Channel" -> "web"): _*)

  def createArrivalNotificationMessage(
    requestData: NodeSeq,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers),
      ec = ec
    )
  }

  def resubmitArrivalNotificationMessage(
    requestData: NodeSeq,
    arrivalId: String,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/$arrivalId"

    http.PUTString[HttpResponse](serviceUrl, requestData.toString)(
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers),
      ec = ec
    )
  }

  def submitMessageToCore(
    requestData: NodeSeq,
    arrivalId: String,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/$arrivalId/messages"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(
      rds = HttpReads[HttpResponse],
      hc = newHeaders(headers),
      ec = ec
    )
  }
}
