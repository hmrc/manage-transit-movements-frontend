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
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyP5DeparturesAPIConnector @Inject() (val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def departureOutbound(requestData: NodeSeq, headers: Headers)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val newHeaders: HeaderCarrier = headerCarrier
      .copy(authorization = headers.get("Authorization").map(Authorization))
      .withExtraHeaders(
        "Content-Type" -> "application/xml",
        ("Accept", "application/vnd.hmrc.2.0+json")
      )

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/departures"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)(rds = HttpReads[HttpResponse], hc = newHeaders, ec = ec)
  }

  def departureInbound(requestData: NodeSeq, departureId: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.transitMovementsUrl}transit-movements/traders/movements/$departureId/messages"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)
  }

  def departureAddMessage(requestData: NodeSeq, departureId: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"

    http.POSTString[HttpResponse](serviceUrl, requestData.toString)
  }
}
